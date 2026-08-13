package ktor.oauth2.multi.tenant.auth.server.app.config

import com.google.gson.Gson
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import ktor.oauth2.multi.tenant.auth.server.storage.service.FileService

fun Application.infoModule(): AppInfo {
    val fileService: FileService by dependencies

    var appInfo = AppInfo(version = "v0.0.0_Dev") // Default version

    fileService.loadTextFile("/app/info.json")?.let { json ->
        appInfo = Gson().fromJson(json, AppInfo::class.java)
    }

    dependencies {
        provide<AppInfo> {
            appInfo
        }
    }
    return appInfo
}
