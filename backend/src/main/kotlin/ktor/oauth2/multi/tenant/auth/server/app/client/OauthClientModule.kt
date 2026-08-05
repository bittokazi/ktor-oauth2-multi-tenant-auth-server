package ktor.oauth2.multi.tenant.auth.server.app.client

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import ktor.oauth2.multi.tenant.auth.server.app.client.controllers.clientRoutes
import ktor.oauth2.multi.tenant.auth.server.app.client.services.ClientService
import ktor.oauth2.multi.tenant.auth.server.app.client.services.DefaultClientService

fun Application.oauthClientModule() {
    dependencies {
        provide<ClientService>(DefaultClientService::class)
    }

    clientRoutes()
}
