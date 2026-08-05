package ktor.oauth2.multi.tenant.auth.server.security.controllers

import com.bittokazi.ktor.auth.utils.getBaseUrl
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.LOGIN_API
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.OAUTH_CALLBACK_API
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.REFRESH_TOKEN_API
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import ktor.oauth2.multi.tenant.auth.server.security.entity.RefreshTokenRequest
import ktor.oauth2.multi.tenant.auth.server.security.services.LoginService
import ktor.oauth2.multi.tenant.auth.server.security.services.LoginServiceErrorCode
import ktor.oauth2.multi.tenant.auth.server.utils.PkceUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Application.configureIdpLoginRoutes() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val loginService: LoginService by dependencies

    routing {
        get(LOGIN_API) {
            when (val result = loginService.loginRedirect(call)) {
                is CallResult.Failure ->
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        message = result,
                    )

                is CallResult.Success -> {
                    val client = result.outcome
                    val codeVerifier = PkceUtils.generateCodeVerifier()
                    val codeChallenge = PkceUtils.generateCodeChallengeS256(codeVerifier)
                    loginService.updatePkceCodeCookies(codeVerifier, call)
                    call.respondRedirect(
                        "/oauth/authorize?client_id=${client.clientId}&response_type=code" +
                            "&scope=${client.scopes.joinToString("+")}" +
                            "&redirect_uri=${call.getBaseUrl()}/api/v1/oauth/callback" +
                            "&code_challenge_method=S256&code_challenge=$codeChallenge",
                    )
                }
            }
        }

        get(OAUTH_CALLBACK_API) {
            when (val result = loginService.fetchToken(call)) {
                is CallResult.Failure -> {
                    when (result.errorCode) {
                        LoginServiceErrorCode.CLIENT_NOT_FOUND ->
                            call.respond(
                                status = HttpStatusCode.NotFound,
                                message = result,
                            )

                        LoginServiceErrorCode.MISSING_CODE ->
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = result,
                            )

                        LoginServiceErrorCode.UNAUTHORIZED ->
                            call.respond(
                                status = HttpStatusCode.Unauthorized,
                                message = result,
                            )

                        LoginServiceErrorCode.BAD_REQUEST ->
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = result,
                            )
                    }
                }

                is CallResult.Success -> {
                    loginService.updateCookies(result.outcome, call)
                    call.respondRedirect("/app/dashboard")
                }
            }
        }

        post(REFRESH_TOKEN_API) {
            val refreshTokenRequest = call.receive<RefreshTokenRequest>()

            when (val result = loginService.fetchRefreshToken(refreshTokenRequest, call)) {
                is CallResult.Failure -> {
                    when (result.errorCode) {
                        LoginServiceErrorCode.CLIENT_NOT_FOUND ->
                            call.respond(
                                status = HttpStatusCode.NotFound,
                                message = result,
                            )

                        LoginServiceErrorCode.MISSING_CODE ->
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = result,
                            )

                        LoginServiceErrorCode.UNAUTHORIZED ->
                            call.respond(
                                status = HttpStatusCode.Unauthorized,
                                message = result,
                            )

                        LoginServiceErrorCode.BAD_REQUEST ->
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = result,
                            )
                    }
                }

                is CallResult.Success -> {
                    loginService.updateCookies(result.outcome, call)
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = result,
                    )
                }
            }
        }
    }

    log.info("[configureIdpLoginRoutes] -> Routes configured.")
}
