package ktor.oauth2.multi.tenant.auth.server.app.tenant

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import ktor.oauth2.multi.tenant.auth.server.app.tenant.controllers.tenantRoute
import ktor.oauth2.multi.tenant.auth.server.app.tenant.services.DefaultTenantService
import ktor.oauth2.multi.tenant.auth.server.app.tenant.services.TenantService

fun Application.tenantModule() {
    dependencies {
        provide<TenantService>(DefaultTenantService::class)
    }

    tenantRoute()
}
