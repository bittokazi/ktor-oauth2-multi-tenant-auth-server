package ktor.oauth2.multi.tenant.auth.server.documentation

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.di.dependencies
import ktor.oauth2.multi.tenant.auth.server.documentation.config.DocumentationConfiguration
import ktor.oauth2.multi.tenant.auth.server.documentation.config.SwaggerAuthentication
import ktor.oauth2.multi.tenant.auth.server.documentation.controllers.swaggerRoutes
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.UserRepository

fun Application.configureDocumentationModule() {
    val userRepository: UserRepository by dependencies

    install(SwaggerAuthentication(userRepository))

    dependencies {
        provide(DocumentationConfiguration::class)
    }

    val documentationConfiguration: DocumentationConfiguration by dependencies

    if (documentationConfiguration.enabled) {
        swaggerRoutes()
    }
}
