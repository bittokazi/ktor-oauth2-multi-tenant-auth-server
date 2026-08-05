package ktor.oauth2.multi.tenant.auth.server.security.config

import com.bittokazi.ktor.auth.OauthUserSession
import com.bittokazi.ktor.auth.services.providers.OauthLoginOptionService
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.util.AttributeKey
import ktor.oauth2.multi.tenant.auth.server.config.AppConfig
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.TenantRepository
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.UserRepository
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.UserTrustedDeviceRepository
import ktor.oauth2.multi.tenant.auth.server.security.entity.OauthRedirectUrlSession
import ktor.oauth2.multi.tenant.auth.server.security.entity.UserTwoFaSession

class LoginOptionServiceImpl(
    val userTrustedDeviceRepository: UserTrustedDeviceRepository,
    val userRepository: UserRepository,
    val tenantRepository: TenantRepository,
) : OauthLoginOptionService {
    override val fallbackAfterLoginRedirectUrl: String = "/app/dashboard"

    override suspend fun isAfterLoginCheckCompleted(
        oauthUserSession: OauthUserSession,
        call: ApplicationCall,
    ): Boolean {
        return when (val session = call.sessions.get<UserTwoFaSession>()) {
            null -> {
                val user = userRepository.findOneById(oauthUserSession.userId, false, call)

                when (user?.twoFaEnabled) {
                    true -> {
                        // If 2FA is enabled, check for trusted device by instanceId cookie
                        val instanceId = call.request.cookies["instanceId"]

                        if (!instanceId.isNullOrBlank()) {
                            val trusted = userTrustedDeviceRepository.findAllByUserIdAndInstanceId(user.id.toString(), instanceId, call)
                            if (trusted != null) {
                                // Trusted device found — set session and proceed
                                val newSession = UserTwoFaSession(user.id.toString(), user.email)
                                call.sessions.set(newSession)
                                return true
                            }
                        }

                        // No trusted device — redirect to otp check
                        call.respondRedirect("/oauth/otp-check")
                        false
                    }
                    else -> {
                        val session = UserTwoFaSession(user?.id.toString(), user!!.email)
                        call.sessions.set(session)
                        true
                    }
                }
            }
            else -> {
                when (session.userId == oauthUserSession.userId) {
                    true -> true
                    else -> {
                        call.sessions.clear("USER_TWO_FA_SESSION")
                        call.respondRedirect("/oauth/otp-check")
                        false
                    }
                }
            }
        }
    }

    override suspend fun onSuccessfulLogin(
        oauthUserSession: OauthUserSession,
        call: ApplicationCall,
    ) {
        call.sessions.clear("USER_TWO_FA_SESSION")
    }

    override suspend fun completeLogin(call: ApplicationCall) {
        val originalUrl = call.sessions.get("OAUTH_ORIGINAL_URL")
        if (originalUrl != null) {
            call.sessions.clear("OAUTH_ORIGINAL_URL")
            call.respondRedirect(originalUrl.toString())
        } else {
            val session = call.sessions.get<OauthRedirectUrlSession>()
            if (session != null) {
                call.sessions.clear("OAUTH2_REDIRECT_URL")
                call.respondRedirect(session.redirectUrl)
            } else {
                val tenant = call.attributes[AttributeKey<String>(AppConfig.TENANT_ATTRIBUTE_KEY)]
                if (tenant == "public") {
                    call.respondRedirect(fallbackAfterLoginRedirectUrl) // fallback
                } else {
                    tenantRepository.findOneByCompanyKey(tenant)?.let {
                        it.defaultRedirectUrl?.let { redirectUrl ->
                            when (redirectUrl) {
                                "" -> call.respondRedirect(fallbackAfterLoginRedirectUrl)
                                else -> call.respondRedirect(redirectUrl)
                            }
                        } ?: call.respondRedirect(fallbackAfterLoginRedirectUrl)
                    } ?: call.respondRedirect(fallbackAfterLoginRedirectUrl)
                }
            }
        }
    }
}
