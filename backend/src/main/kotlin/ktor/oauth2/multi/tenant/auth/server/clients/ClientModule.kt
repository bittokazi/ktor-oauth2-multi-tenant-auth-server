package ktor.oauth2.multi.tenant.auth.server.clients

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import ktor.oauth2.multi.tenant.auth.server.clients.idp.IdpClient
import ktor.oauth2.multi.tenant.auth.server.clients.idp.IdpClientConfig
import ktor.oauth2.multi.tenant.auth.server.security.controllers.configureIdpLoginRoutes

fun Application.configureClientModule() {
    dependencies {
        provide(IdpClientConfig::class)
    }

    val idpClientConfig: IdpClientConfig by dependencies

    dependencies {
        if (idpClientConfig.enabled) {
            provide(IdpClient::class)
        }
    }

    if (idpClientConfig.enabled) {
        configureIdpLoginRoutes()
    }
}
