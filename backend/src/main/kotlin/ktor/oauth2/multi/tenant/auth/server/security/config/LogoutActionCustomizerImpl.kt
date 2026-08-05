package ktor.oauth2.multi.tenant.auth.server.security.config

import com.bittokazi.ktor.auth.services.providers.OauthLogoutActionService
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.sessions

class LogoutActionCustomizerImpl(
    val oauthConfig: OauthConfig,
) : OauthLogoutActionService {
    override suspend fun afterLogoutAction(
        userId: String?,
        call: ApplicationCall,
    ) {
        call.sessions.clear("USER_TWO_FA_SESSION")

        val clientId = call.request.queryParameters["client_id"]

        if (clientId != null && oauthConfig.noRedirectClients.contains(clientId)) {
            call.respond("Logout Successful")
        } else {
            call.respondRedirect("/app/login")
        }
    }
}
