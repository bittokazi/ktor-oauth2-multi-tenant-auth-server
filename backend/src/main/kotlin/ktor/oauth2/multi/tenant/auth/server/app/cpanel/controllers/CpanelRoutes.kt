package ktor.oauth2.multi.tenant.auth.server.app.cpanel.controllers

import io.ktor.server.application.Application
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.mustache.MustacheContent
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import ktor.oauth2.multi.tenant.auth.server.app.config.AppModuleConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

fun Application.cpanelRoutes() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val appModuleConfiguration: AppModuleConfiguration by dependencies

    routing {
        staticResources("/app-static", "static/cpanel")
        staticFiles("/app-cpanel-static", File(appModuleConfiguration.templateFolder))
    }

    routing {
        // ignore!
        get("/app") {
            call.respond(MustacheContent("cpanel.hbs", mapOf<String, String>()))
        }

        // ignore!
        get("/app/") {
            call.respond(MustacheContent("cpanel.hbs", mapOf<String, String>()))
        }

        // ignore!
        get("/app/{...}") {
            call.respond(MustacheContent("cpanel.hbs", mapOf<String, String>()))
        }
    }

    log.info("[cpanelRoutes] -> Routes configured.")
}
