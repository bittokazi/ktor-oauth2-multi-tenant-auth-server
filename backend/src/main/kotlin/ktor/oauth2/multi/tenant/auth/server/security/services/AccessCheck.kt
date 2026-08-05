package ktor.oauth2.multi.tenant.auth.server.security.services

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
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
