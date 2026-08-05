package ktor.oauth2.multi.tenant.auth.server.app.user

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import ktor.oauth2.multi.tenant.auth.server.app.user.controllers.userRoute
import ktor.oauth2.multi.tenant.auth.server.app.user.services.DefaultUserService
import ktor.oauth2.multi.tenant.auth.server.app.user.services.UserService

fun Application.userModule() {
    dependencies {
        provide<UserService>(DefaultUserService::class)
    }

    userRoute()
}
