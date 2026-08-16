package ktor.oauth2.multi.tenant.auth.server.security.config

import com.bittokazi.ktor.auth.config.Oauth2Config
import com.bittokazi.ktor.auth.services.providers.OauthClientService
import com.bittokazi.ktor.auth.services.providers.OauthLogoutActionService
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.sessions

class LogoutActionCustomizerImpl(
    val oauthConfig: OauthConfig,
    val oauth2Config: Oauth2Config,
    val oauthClientService: OauthClientService,
) : OauthLogoutActionService {
    override suspend fun afterLogoutAction(
        userId: String?,
        clientId: String?,
        call: ApplicationCall,
    ) {
        call.sessions.clear("USER_TWO_FA_SESSION")

        val client = clientId?.let { oauthClientService.findByClientId(it, call) }
        val redirectUrl = client?.postLogoutRedirectUri ?: oauth2Config.afterLogoutRedirectUrl

        if (clientId != null && oauthConfig.noRedirectClients.contains(clientId)) {
            call.respond("Logout Successful")
        } else {
            call.respondRedirect(redirectUrl)
        }
    }
}
