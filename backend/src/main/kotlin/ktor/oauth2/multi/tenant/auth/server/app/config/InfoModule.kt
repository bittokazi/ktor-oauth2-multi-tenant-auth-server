package ktor.oauth2.multi.tenant.auth.server.app.config

import com.google.gson.Gson
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.plugins.di.dependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ktor.oauth2.multi.tenant.auth.server.clients.github.GithubClient
import ktor.oauth2.multi.tenant.auth.server.storage.service.FileService

fun Application.infoModule(): AppInfo {
    val fileService: FileService by dependencies
    val githubClient: GithubClient by dependencies

    var appInfo = AppInfo(version = "v0.0.0_Dev") // Default version

    fileService.loadTextFile("/app/info.json")?.let { json ->
        appInfo = Gson().fromJson(json, AppInfo::class.java)
    } ?: run {
        fileService.loadTextFile("info.json")?.let { json ->
            appInfo = Gson().fromJson(json, AppInfo::class.java)
        }
    }

    CoroutineScope(Dispatchers.Default).launch {
        githubClient.getReleaseChangeLog(appInfo.version)?.let { changeLogs ->
            val appInfo: AppInfo by dependencies
            appInfo.changeLogs = changeLogs.body
        }
        log.info("[InfoModule] -> Loaded app info: $appInfo")
    }

    dependencies {
        provide<AppInfo> {
            appInfo
        }
    }
    return appInfo
}
