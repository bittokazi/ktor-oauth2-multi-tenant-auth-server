package ktor.oauth2.multi.tenant.auth.server.security.config

import com.bittokazi.ktor.auth.services.SessionExtender
import io.ktor.server.plugins.di.annotations.Property
import io.ktor.server.sessions.SessionTransportTransformerEncrypt
import io.ktor.server.sessions.SessionsConfig
import io.ktor.server.sessions.cookie
import io.ktor.util.hex
import ktor.oauth2.multi.tenant.auth.server.security.entity.OauthRedirectUrlSession
import ktor.oauth2.multi.tenant.auth.server.security.entity.UserTwoFaSession
import kotlin.random.Random

class SessionExtenderImpl(
    @Property("oauth.session.encryption-key") val encryptionKey: String? = null,
    @Property("oauth.session.signing-key") val signingKey: String? = null,
) : SessionExtender {
    override fun extent(sessionsConfig: SessionsConfig) {
        var secretEncryptKey = hex(Random.nextBytes(16).joinToString("") { "%02x".format(it) }) // 16 bytes = AES128
        var secretSignKey = hex(Random.nextBytes(16).joinToString("") { "%02x".format(it) }) // 16 bytes

        if (encryptionKey != null || signingKey != null) {
            secretEncryptKey = hex(encryptionKey!!)
            secretSignKey = hex(signingKey!!)
        }

        sessionsConfig.cookie<UserTwoFaSession>("USER_TWO_FA_SESSION") {
            cookie.httpOnly = true
            cookie.secure = false
            cookie.maxAgeInSeconds = 315360000
            transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey))
        }

        sessionsConfig.cookie<OauthRedirectUrlSession>("OAUTH2_REDIRECT_URL") {
            cookie.httpOnly = true
            cookie.secure = false // set true in production (HTTPS only)
        }
    }
}
