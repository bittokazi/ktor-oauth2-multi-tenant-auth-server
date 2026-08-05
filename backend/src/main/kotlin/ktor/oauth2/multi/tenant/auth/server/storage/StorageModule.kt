package ktor.oauth2.multi.tenant.auth.server.storage

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import ktor.oauth2.multi.tenant.auth.server.storage.service.DefaultFileService
import ktor.oauth2.multi.tenant.auth.server.storage.service.FileService

fun Application.configureStorageModule() {
    dependencies {
        provide<FileService>(DefaultFileService::class)
    }
}
