package ktor.oauth2.multi.tenant.auth.server

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import ktor.oauth2.multi.tenant.auth.server.app.configureAppModule
import ktor.oauth2.multi.tenant.auth.server.clients.configureClientModule
import ktor.oauth2.multi.tenant.auth.server.database.configureDatabase
import ktor.oauth2.multi.tenant.auth.server.documentation.configureDocumentationModule
import ktor.oauth2.multi.tenant.auth.server.health.configureHealthModule
import ktor.oauth2.multi.tenant.auth.server.mail.configureMailModule
import ktor.oauth2.multi.tenant.auth.server.persistence.configurePersistenceModule
import ktor.oauth2.multi.tenant.auth.server.security.configureSecurityModule
import ktor.oauth2.multi.tenant.auth.server.storage.configureStorageModule

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureDatabase()
    configurePersistenceModule()
    configureSecurityModule()
    configureClientModule()
    configureHealthModule()
    configureMailModule()
    configureStorageModule()
    configureAppModule()
    configureDocumentationModule()
    configureHTTP()
}
