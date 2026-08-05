package ktor.oauth2.multi.tenant.auth.server.documentation.controllers

import io.ktor.http.ContentType
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.application.Application
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.routing
import io.ktor.server.routing.routingRoot
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Application.swaggerRoutes() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    routing {
        swaggerUI("/swagger") {
            info = OpenApiInfo("Ktor Authorization Server API", "1.0")
            source =
                OpenApiDocSource.Routing(ContentType.Application.Json) {
                    routingRoot
                        .descendants()
                        .filter { route ->
                            (!route.toString().contains("/password-reset")) &&
                                (
                                    route.toString().contains("/users") ||
                                        route.toString().contains("/roles") ||
                                        route.toString().contains("/clients") ||
                                        route.toString().contains("/tenants")
                                )
                        }
                }
        }
    }

    log.info("[swaggerRoutes] -> Routes configured.")
}
