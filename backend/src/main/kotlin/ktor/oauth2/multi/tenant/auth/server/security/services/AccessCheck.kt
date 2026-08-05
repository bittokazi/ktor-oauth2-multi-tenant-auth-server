package ktor.oauth2.multi.tenant.auth.server.security.services

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.ScopeEnum

suspend fun scopeCheck(
    scopes: List<ScopeEnum>,
    call: ApplicationCall,
    block: suspend () -> Unit,
) {
    val errorResponse =
        suspend {
            call.respond(
                status = HttpStatusCode.Forbidden,
                message =
                    mapOf(
                        "error" to "forbidden",
                        "status" to "FORBIDDEN/403",
                        "message" to "Access denied!",
                    ),
            )
        }

    val result =
        runCatching {
            var actorScopes = emptyList<String>()

            call.principal<JWTPrincipal>()?.let { principal ->
                actorScopes =
                    principal
                        .payload
                        .claims["scope"]
                        .toString()
                        .replace("\"", "")
                        .split(" ")
            }

            scopes.filter {
                actorScopes.contains(it.name)
            }.size
        }

    when {
        result.isSuccess ->
            when {
                result.getOrNull()!! > 0 -> block()
                else -> errorResponse()
            }
        else -> errorResponse()
    }
}
