package ktor.oauth2.multi.tenant.auth.server.health.controllers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import ktor.oauth2.multi.tenant.auth.server.health.services.HealthService
import ktor.oauth2.multi.tenant.auth.server.health.services.HealthServiceErrorCode
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Application.healthRoutes() {
    val log: Logger = LoggerFactory.getLogger(javaClass)
    val healthService: HealthService by dependencies

    routing {
        get("/health") {
            when (val result = healthService.checkHealth(call)) {
                is CallResult.Success -> {
                    call.respond(
                        HttpStatusCode.OK,
                        result.outcome,
                    )
                }

                is CallResult.Failure -> {
                    handleHealthErrorResponse(result)
                }
            }
        }
    }

    log.info("[healthRoutes] -> Routes configured.")
}

private suspend fun RoutingContext.handleHealthErrorResponse(callResult: CallResult.Failure<*, HealthServiceErrorCode>) {
    when (callResult.errorCode) {
        HealthServiceErrorCode.DATABASE_UNAVAILABLE ->
            call.respond(
                status = HttpStatusCode.ServiceUnavailable,
                message = callResult,
            )

        HealthServiceErrorCode.HEALTH_CHECK_FAILED ->
            call.respond(
                status = HttpStatusCode.ServiceUnavailable,
                message = callResult,
            )
    }
}
