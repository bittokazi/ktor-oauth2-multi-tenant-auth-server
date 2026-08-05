package ktor.oauth2.multi.tenant.auth.server.utils

import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.math.BigInteger
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.util.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object Utils {
    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    @JvmStatic
    fun randomNumberGenerator(length: Int): String {
        val saltChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz"
        val salt = StringBuilder()
        val rnd = Random()
        while (salt.length < length) {
            val index = (rnd.nextFloat() * saltChar.length).toInt()
            salt.append(saltChar[index])
        }
        val saltStr = salt.toString()
        return saltStr
    }

    @JvmStatic
    fun referenceGenerator(length: Int): String {
        val saltChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
        val salt = StringBuilder()
        val rnd = Random()
        while (salt.length < length) {
            val index = (rnd.nextFloat() * saltChar.length).toInt()
            salt.append(saltChar[index])
        }
        val saltStr = salt.toString()
        return saltStr
    }

    fun numberGenerator(length: Int): String {
        val saltChar = "1234567890"
        val salt = StringBuilder()
        val rnd = Random()
        while (salt.length < length) {
            val index = (rnd.nextFloat() * saltChar.length).toInt()
            salt.append(saltChar[index])
        }
        val saltStr = salt.toString()
        return saltStr
    }

    @OptIn(ExperimentalTime::class)
    fun isLessThan30DaysAway(target: Instant): Boolean {
        val now = Clock.System.now()
        val limit = now + 30.days
        return target <= limit
    }

    fun loadFile(path: String): String {
        val file = File(path)

        // 1) Absolute or relative file on host filesystem
        if (file.exists()) {
            return file.readText()
        }

        // 2) Resource inside the JAR (from src/main/resources)
        val resourceStream =
            object {}.javaClass.classLoader.getResourceAsStream(path)
                ?: error("File '$path' not found on filesystem or resources")

        return resourceStream.bufferedReader().readText()
    }

    @OptIn(ExperimentalTime::class)
    fun getCurrentYearMonth(): String {
        val localDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val year = localDateTime.year // 2026
        val month = localDateTime.month // MAY
        val monthNumber = localDateTime.month.number // 5
        return "$year-$monthNumber"
    }

    fun isPublicTenant(call: ApplicationCall): Boolean {
        return call.attributes[AttributeKey<String>("tenant")].let { tenant ->
            tenant == "public"
        }
    }
}

fun String.toPrivateKey(): RSAPrivateKey {
    // Remove PEM header/footer
    val privateKeyPem =
        this
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

    val pkcs8EncodedBytes = Base64.getDecoder().decode(privateKeyPem)

    val keySpec = PKCS8EncodedKeySpec(pkcs8EncodedBytes)
    val keyFactory = KeyFactory.getInstance("RSA")
    return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
}
