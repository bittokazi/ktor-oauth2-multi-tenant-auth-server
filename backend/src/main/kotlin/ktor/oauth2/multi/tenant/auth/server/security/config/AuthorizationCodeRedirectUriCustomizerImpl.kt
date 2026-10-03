package ktor.oauth2.multi.tenant.auth.server.security.config

import com.bittokazi.ktor.auth.services.authorization.AuthorizationCodeRedirectUriCustomizer
import io.ktor.http.URLBuilder
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.di.annotations.Property
import kotlinx.serialization.Serializable
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.NonceMapEntity
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.NonceMapRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AuthorizationCodeRedirectUriCustomizerImpl(
    private val config: AuthorizationCodeRedirectUriCustomizerConfig,
    private val nonceMapRepository: NonceMapRepository,
) : AuthorizationCodeRedirectUriCustomizer {
    var log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[AuthorizationCodeRedirectUriCustomizerImpl] -> init")
    }

    override fun customizeRedirectUri(
        redirectUri: String,
        call: ApplicationCall,
    ): String {
        var redirectUri = redirectUri

        config.passQueryParams.forEach { param ->
            call.request.queryParameters[param]?.let { value ->
                URLBuilder(redirectUri).build().parameters["code"]?.let { code ->
                    nonceMapRepository.insert(NonceMapEntity(code, value), call)
                }
                redirectUri += "&$param=$value"
            }
        }

        return redirectUri
    }
}

@Serializable
data class AuthorizationCodeRedirectUriCustomizerConfig(
    @Property("auth-code-redirect-uri-customizer.pass-query-parameters") val passQueryParams: List<String> = listOf(),
)
