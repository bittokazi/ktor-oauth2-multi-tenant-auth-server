package ktor.oauth2.multi.tenant.auth.server.health

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import ktor.oauth2.multi.tenant.auth.server.health.config.HealthConfiguration
import ktor.oauth2.multi.tenant.auth.server.health.controllers.healthRoutes
import ktor.oauth2.multi.tenant.auth.server.health.services.DefaultHealthService
import ktor.oauth2.multi.tenant.auth.server.health.services.HealthService

fun Application.configureHealthModule() {
    dependencies {
        provide(HealthConfiguration::class)
    }

    val healthConfiguration: HealthConfiguration by dependencies

    if (healthConfiguration.enabled) {
        dependencies {
            provide<HealthService>(DefaultHealthService::class)
        }

        healthRoutes()
    }
}
