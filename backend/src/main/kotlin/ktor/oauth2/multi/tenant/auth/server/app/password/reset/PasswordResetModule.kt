package ktor.oauth2.multi.tenant.auth.server.app.password.reset

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import ktor.oauth2.multi.tenant.auth.server.app.password.reset.controllers.passwordResetRoutes
import ktor.oauth2.multi.tenant.auth.server.app.password.reset.services.DefaultPasswordResetService
import ktor.oauth2.multi.tenant.auth.server.app.password.reset.services.PasswordResetService

fun Application.passwordResetModule() {
    dependencies {
        provide<PasswordResetService>(DefaultPasswordResetService::class)
    }

    passwordResetRoutes()
}
