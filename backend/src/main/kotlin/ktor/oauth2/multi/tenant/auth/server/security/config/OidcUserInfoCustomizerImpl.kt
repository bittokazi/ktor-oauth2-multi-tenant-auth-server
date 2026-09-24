package ktor.oauth2.multi.tenant.auth.server.security.config

import com.bittokazi.ktor.auth.services.oidc.OidcUserInfoCustomizer
import com.nimbusds.jwt.JWTClaimsSet
import io.ktor.server.application.ApplicationCall
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OidcUserInfoCustomizerImpl : OidcUserInfoCustomizer {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[OidcUserInfoCustomizerImpl] -> init")
    }

    override fun customize(
        userInfo: MutableMap<String, Any>,
        claims: JWTClaimsSet,
        call: ApplicationCall,
    ): MutableMap<String, Any> {
        // Customize the user info as needed
        userInfo["email_verified"] = true
        return userInfo
    }
}
