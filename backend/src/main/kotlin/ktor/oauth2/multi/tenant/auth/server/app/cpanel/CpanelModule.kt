package ktor.oauth2.multi.tenant.auth.server.app.cpanel

import io.ktor.server.application.Application
import ktor.oauth2.multi.tenant.auth.server.app.cpanel.controllers.cpanelRoutes

fun Application.cpanelModule() {
    cpanelRoutes()
}
