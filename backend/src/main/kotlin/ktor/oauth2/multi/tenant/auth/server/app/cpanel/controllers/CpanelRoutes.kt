package ktor.oauth2.multi.tenant.auth.server.app.cpanel.controllers

import com.bittokazi.ktor.auth.services.TemplateCustomizerFactory
import com.bittokazi.ktor.auth.utils.respondMustache
import com.github.mustachejava.DefaultMustacheFactory
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import ktor.oauth2.multi.tenant.auth.server.app.config.AppModuleConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.StringWriter

fun Application.cpanelRoutes() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val appModuleConfiguration: AppModuleConfiguration by dependencies
    val templateCustomizerFactory: TemplateCustomizerFactory by dependencies

    routing {
        staticResources("/app-static", "static/cpanel")
        staticFiles("/app-cpanel-static", File(appModuleConfiguration.templateFolder))
    }

    routing {
        get("/app") {
            respondCpanelTemplate(
                call,
                "cpanel.hbs",
                mapOf<String, Any>(),
            )
        }

        get("/app/") {
            respondCpanelTemplate(
                call,
                "cpanel.hbs",
                mapOf<String, Any>(),
            )
        }

        get("/app/{...}") {
            respondCpanelTemplate(
                call,
                "cpanel.hbs",
                mapOf<String, Any>(),
            )
        }

        get("/app/{...}") {
            call.respondMustache(
                templateCustomizerFactory,
                "cpanel.hbs",
                mapOf<String, Any>(),
            )
        }
    }

    log.info("[cpanelRoutes] -> Routes configured.")
}

private suspend fun respondCpanelTemplate(
    call: ApplicationCall,
    template: String,
    model: Any,
) {
    val factory = DefaultMustacheFactory("templates")

    val html =
        StringWriter().also {
            factory.compile(template)
                .execute(it, model)
                .flush()
        }.toString()

    call.respondText(html, ContentType.Text.Html)
}
