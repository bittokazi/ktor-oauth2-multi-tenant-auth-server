package ktor.oauth2.multi.tenant.auth.server.clients.idp

import com.bittokazi.ktor.auth.domains.token.OauthTokenResponse
import com.bittokazi.ktor.auth.services.providers.OAuthClientDTO
import com.bittokazi.ktor.auth.utils.getBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.di.annotations.Property
import kotlinx.serialization.json.Json
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import ktor.oauth2.multi.tenant.auth.server.security.entity.RefreshTokenRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class IdpClient {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    private val applicationHttpClient: HttpClient =
        HttpClient(Apache) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
        }

    init {
        log.info("[IdpClient] -> created")
    }

    suspend fun fetchAccessToken(
        code: String,
        client: OAuthClientDTO,
        call: ApplicationCall,
    ): CallResult<OauthTokenResponse, IdpClientErrorCode> {
        val response =
            applicationHttpClient.submitForm(
                url = "${call.getBaseUrl()}/oauth/token",
                formParameters =
                    Parameters.build {
                        append("grant_type", "authorization_code")
                        append("code", code)
                        append("redirect_uri", "${call.getBaseUrl()}/api/v1/oauth/callback")
                        append("client_id", client.clientId)
                        append("code_verifier", call.request.cookies["code_verifier"] ?: "")
                    },
            )

        return when (response.status) {
            HttpStatusCode.OK -> CallResult.Success(response.body<OauthTokenResponse>())
            HttpStatusCode.Unauthorized -> CallResult.Failure(IdpClientErrorCode.UNAUTHORIZED)
            else -> CallResult.Failure(IdpClientErrorCode.BAD_REQUEST)
        }
    }

    suspend fun fetchRefreshToken(
        refreshTokenRequest: RefreshTokenRequest,
        client: OAuthClientDTO,
        call: ApplicationCall,
    ): CallResult<OauthTokenResponse, IdpClientErrorCode> {
        val response: HttpResponse =
            applicationHttpClient.submitForm(
                url = "${call.getBaseUrl()}/oauth/token",
                formParameters =
                    Parameters.build {
                        append("grant_type", "refresh_token")
                        append("refresh_token", refreshTokenRequest.refreshToken)
                        append("client_id", client.clientId)
                    },
            )

        return when (response.status) {
            HttpStatusCode.OK -> CallResult.Success(response.body<OauthTokenResponse>())
            HttpStatusCode.Unauthorized -> CallResult.Failure(IdpClientErrorCode.UNAUTHORIZED)
            else -> CallResult.Failure(IdpClientErrorCode.BAD_REQUEST)
        }
    }
}

data class IdpClientConfig(
    @Property("idp-client-config.enabled") val enabled: Boolean,
)

enum class IdpClientErrorCode {
    UNAUTHORIZED,
    BAD_REQUEST,
}
