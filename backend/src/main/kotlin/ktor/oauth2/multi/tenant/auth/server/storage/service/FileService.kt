package ktor.oauth2.multi.tenant.auth.server.storage.service

import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import ktor.oauth2.multi.tenant.auth.server.app.config.AppModuleConfiguration
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

interface FileService {
    suspend fun upload(
        tenant: String,
        call: ApplicationCall,
        fn: suspend (CallResult<String, FileErrorCode>) -> Unit,
    )

    fun loadTextFile(path: String): String?
}

class DefaultFileService(
    val appModuleConfiguration: AppModuleConfiguration,
) : FileService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[FileService] -> init")
    }

    private val extensions =
        listOf(
            "zip",
        )

    override suspend fun upload(
        tenant: String,
        call: ApplicationCall,
        fn: suspend (CallResult<String, FileErrorCode>) -> Unit,
    ) {
        // retrieve all multipart data (suspending)
        val multipart = call.receiveMultipart()
        multipart.forEachPart { part ->
            // if part is a file (could be form item)
            if (part is PartData.FileItem) {
                val extension = part.originalFileName?.split(".")?.last()

                if (extensions.contains(extension)) {
                    // retrieve file name of upload
                    val folder = appModuleConfiguration.templateFolder

                    // use InputStream from part to save file
                    val fileName = part.save(folder, "$tenant.$extension")

                    when (unzip(folder, tenant, fileName)) {
                        true -> fn(CallResult.Success("$folder${File.separator}$tenant"))
                        false ->
                            fn(
                                CallResult.Failure(
                                    errorCode = FileErrorCode.INTERNAL_ERROR,
                                ),
                            )
                    }
                } else {
                    fn(
                        CallResult.Failure(
                            errorCode = FileErrorCode.UNSUPPORTED_EXTENSION,
                        ),
                    )
                }
            }
            // make sure to dispose of the part after use to prevent leaks
            part.dispose()
        }
    }

    suspend fun PartData.FileItem.save(
        path: String,
        fileName: String,
    ): String {
        val file = File("$path${File.separator}$fileName")
        if (file.exists()) {
            file.delete()
        }
        log.info("Path = $path/$fileName")
        provider().copyAndClose(File("$path${File.separator}$fileName").writeChannel())
        return fileName
    }

    fun unzip(
        folder: String,
        tenant: String,
        fileName: String,
    ): Boolean {
        val bytes: ByteArray
        try {
            bytes = File("$folder${File.separator}$fileName").readBytes()
            val fileZip = folder + File.separator + fileName

            if (Files.exists(Paths.get(fileZip.replace(".zip", "")))) {
                File(fileZip.replace(".zip", "")).deleteRecursively()
            }

            if (!Files.exists(Paths.get("$folder${File.separator}$tenant"))) {
                Files.createDirectories(Paths.get("$folder${File.separator}$tenant"))
            }

            val path: Path = Paths.get(fileZip)
            Files.write(path, bytes)

            val destDir: File = File(folder + File.separator + tenant)
            val buffer = ByteArray(1024)
            val zis = ZipInputStream(FileInputStream(fileZip))
            var zipEntry = zis.nextEntry
            while (zipEntry != null) {
                val newFile: File = newFile(destDir, zipEntry)
                if (zipEntry.isDirectory) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw IOException("Failed to create directory $newFile")
                    }
                } else {
                    // fix for Windows-created archives
                    val parent: File = newFile.getParentFile()
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw IOException("Failed to create directory $parent")
                    }

                    // write file content
                    val fos: FileOutputStream = FileOutputStream(newFile)
                    var len: Int
                    while ((zis.read(buffer).also { len = it }) > 0) {
                        fos.write(buffer, 0, len)
                    }
                    fos.close()
                }
                zipEntry = zis.nextEntry
            }
            zis.closeEntry()
            zis.close()
            File(fileZip).delete()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        return true
    }

    @Throws(IOException::class)
    fun newFile(
        destinationDir: File,
        zipEntry: ZipEntry,
    ): File {
        val destFile = File(destinationDir, zipEntry.name)

        val destDirPath = destinationDir.canonicalPath
        val destFilePath = destFile.canonicalPath

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw IOException("Entry is outside of the target dir: " + zipEntry.name)
        }

        return destFile
    }

    override fun loadTextFile(path: String): String? {
        val file = File(path)

        // 1) Absolute or relative file on host filesystem
        if (file.exists()) {
            return file.readText()
        }

        // 2) Resource inside the JAR (from src/main/resources)
        val resourceStream =
            object {}.javaClass.classLoader.getResourceAsStream(path)
                ?: run {
                    log.error("Resource not found: $path")
                    return null
                }

        return resourceStream.bufferedReader().readText()
    }
}

enum class FileErrorCode {
    UNSUPPORTED_EXTENSION,
    INTERNAL_ERROR,
    TENANT_NOT_FOUND,
    UNPROCESSABLE_ENTITY,
}
