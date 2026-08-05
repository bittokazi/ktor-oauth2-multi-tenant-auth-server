package ktor.oauth2.multi.tenant.auth.server.app.role

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import ktor.oauth2.multi.tenant.auth.server.app.role.controllers.roleRoutes
import ktor.oauth2.multi.tenant.auth.server.app.role.services.DefaultRoleService
import ktor.oauth2.multi.tenant.auth.server.app.role.services.RoleService

fun Application.roleModule() {
    dependencies {
        provide<RoleService>(DefaultRoleService::class)
    }

    roleRoutes()
}
