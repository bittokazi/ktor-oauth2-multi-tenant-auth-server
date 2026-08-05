package ktor.oauth2.multi.tenant.auth.server.documentation.config

import com.bittokazi.ktor.auth.OauthUserSession
import com.bittokazi.ktor.auth.services.isValidUserSession
import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.request.path
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.RoleEnum
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.UserRepository
import ktor.oauth2.multi.tenant.auth.server.security.entity.OauthRedirectUrlSession

val SwaggerAuthentication: (UserRepository) -> ApplicationPlugin<Unit> = { userRepository ->
    createApplicationPlugin("SwaggerAuthenticationPlugin") {

        onCall { call ->
            val path = call.request.path()

            if (path.startsWith("/swagger")) {
                if (!isValidUserSession(call)) {
                    val session = OauthRedirectUrlSession(call.request.uri)
                    call.sessions.set(session)
                    call.respondRedirect("/oauth/login")
                    return@onCall
                } else {
                    val session = call.sessions.get<OauthUserSession>()

                    val user = userRepository.findOneById(session?.userId ?: "", false, call)
                    user?.roles?.find { it.roleKey == RoleEnum.ROLE_SUPER_ADMIN.name } ?: run {
                        call.respond(
                            message = "You do not have permission to access this resource.",
                            status = io.ktor.http.HttpStatusCode.Forbidden,
                        )
                        return@onCall
                    }
                }
            }
        }
    }
}
