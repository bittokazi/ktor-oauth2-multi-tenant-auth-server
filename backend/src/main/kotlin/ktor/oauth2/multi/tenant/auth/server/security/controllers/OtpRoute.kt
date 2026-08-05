package ktor.oauth2.multi.tenant.auth.server.security.controllers

import com.bittokazi.ktor.auth.OauthUserSession
import com.bittokazi.ktor.auth.services.TemplateCustomizerFactory
import com.bittokazi.ktor.auth.services.providers.OauthLoginOptionService
import com.bittokazi.ktor.auth.services.userSessionCheck
import com.bittokazi.ktor.auth.utils.respondMustache
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.utils.io.ExperimentalKtorApi
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_TRUSTED_DEVICES
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_TRUSTED_DEVICE_BY_INSTANCE_ID
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_TWO_FA_DISABLE
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_TWO_FA_ENABLE
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_TWO_FA_SCRATCH_CODE
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_TWO_FA_SECRET
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.ScopeEnum
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.UserData
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.UserTrustedDeviceData
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.UserRepository
import ktor.oauth2.multi.tenant.auth.server.security.entity.TwoFASecretPayload
import ktor.oauth2.multi.tenant.auth.server.security.entity.UserTwoFaSession
import ktor.oauth2.multi.tenant.auth.server.security.services.TwoFaService
import ktor.oauth2.multi.tenant.auth.server.security.services.TwoFaServiceError
import ktor.oauth2.multi.tenant.auth.server.security.services.scopeCheck
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@OptIn(ExperimentalKtorApi::class)
fun Application.otpRoute() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val oauthLoginOptionService: OauthLoginOptionService by dependencies
    val twoFaService: TwoFaService by dependencies
    val userRepository: UserRepository by dependencies
    val templateCustomizerFactory: TemplateCustomizerFactory by dependencies

    routing {
        authenticate {
            get(API_TWO_FA_SECRET) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_TWO_FA_ALL,
                    ),
                    call,
                ) {
                    when (val result = twoFaService.generateSecret(call)) {
                        is CallResult.Failure -> handleFailureResponse(call, result)
                        is CallResult.Success -> call.respond(result.outcome)
                    }
                }
            }.describe {
                summary = "Generate 2FA Secret"
                description = "Generates a new Two-Factor Authentication secret for the user."
                tag("Two-Factor Authentication")
                security {
                    oauth2(ScopeEnum.USER_TWO_FA_ALL.name)
                }
                responses {
                    HttpStatusCode.OK {
                        description = "2FA secret generated successfully"
                        schema = jsonSchema<TwoFASecretPayload>()
                    }
                    HttpStatusCode.NotFound {
                        description = "User not found"
                    }
                }
            }

            post(API_TWO_FA_ENABLE) {
                val payload = call.receive<TwoFASecretPayload>()

                scopeCheck(
                    listOf(
                        ScopeEnum.USER_TWO_FA_ALL,
                    ),
                    call,
                ) {
                    when (val result = twoFaService.enable2FA(payload, call)) {
                        is CallResult.Failure -> handleFailureResponse(call, result)
                        is CallResult.Success -> call.respond(result.outcome)
                    }
                }
            }.describe {
                summary = "Enable 2FA"
                description = "Enables Two-Factor Authentication for the user."
                tag("Two-Factor Authentication")
                security {
                    oauth2(ScopeEnum.USER_TWO_FA_ALL.name)
                }
                requestBody {
                    schema = jsonSchema<TwoFASecretPayload>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "2FA enabled successfully"
                        schema = jsonSchema<UserData>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "2FA already enabled or invalid payload"
                    }
                    HttpStatusCode.NotFound {
                        description = "User not found"
                    }
                }
            }

            get(API_TWO_FA_DISABLE) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_TWO_FA_ALL,
                    ),
                    call,
                ) {
                    when (val result = twoFaService.disable(call)) {
                        is CallResult.Failure -> handleFailureResponse(call, result)
                        is CallResult.Success -> call.respond(result.outcome)
                    }
                }
            }.describe {
                summary = "Disable 2FA"
                description = "Disables Two-Factor Authentication for the user."
                tag("Two-Factor Authentication")
                security {
                    oauth2(ScopeEnum.USER_TWO_FA_ALL.name)
                }
                responses {
                    HttpStatusCode.OK {
                        description = "2FA disabled successfully"
                        schema = jsonSchema<UserData>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "2FA not enabled"
                    }
                    HttpStatusCode.NotFound {
                        description = "User not found"
                    }
                }
            }

            get(API_TWO_FA_SCRATCH_CODE) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_TWO_FA_ALL,
                    ),
                    call,
                ) {
                    when (val result = twoFaService.generateScratchCodes(call)) {
                        is CallResult.Failure -> handleFailureResponse(call, result)
                        is CallResult.Success -> call.respond(result.outcome)
                    }
                }
            }.describe {
                summary = "Generate Scratch Codes"
                description = "Generates new scratch codes for Two-Factor Authentication."
                tag("Two-Factor Authentication")
                security {
                    oauth2(ScopeEnum.USER_TWO_FA_ALL.name)
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Scratch codes generated successfully"
                        schema = jsonSchema<List<String>>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "2FA not enabled"
                    }
                    HttpStatusCode.NotFound {
                        description = "User not found"
                    }
                }
            }

            get(API_TRUSTED_DEVICES) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_TWO_FA_ALL,
                    ),
                    call,
                ) {
                    when (val result = twoFaService.getTrustedDevices(call)) {
                        is CallResult.Failure -> handleFailureResponse(call, result)
                        is CallResult.Success -> call.respond(result.outcome)
                    }
                }
            }.describe {
                summary = "Get Trusted Devices"
                description = "Retrieves a list of all trusted devices for the user."
                tag("Two-Factor Authentication")
                security {
                    oauth2(ScopeEnum.USER_TWO_FA_ALL.name)
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Trusted devices retrieved successfully"
                        schema = jsonSchema<List<UserTrustedDeviceData>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "User not found"
                    }
                }
            }

            delete(API_TRUSTED_DEVICE_BY_INSTANCE_ID) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_TWO_FA_ALL,
                    ),
                    call,
                ) {
                    when (val result = twoFaService.deleteTrustedDevices(call)) {
                        is CallResult.Failure -> handleFailureResponse(call, result)
                        is CallResult.Success -> call.respond(result.outcome)
                    }
                }
            }.describe {
                summary = "Delete Trusted Device"
                description = "Removes a trusted device from the user's account."
                tag("Two-Factor Authentication")
                security {
                    oauth2(ScopeEnum.USER_TWO_FA_ALL.name)
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Trusted device deleted successfully"
                        schema = jsonSchema<UserTrustedDeviceData>()
                    }
                    HttpStatusCode.NotFound {
                        description = "Device or user not found"
                    }
                }
            }
        }

        get("/oauth/otp-check") {
            userSessionCheck(call) {
                val session = call.sessions.get<UserTwoFaSession>()
                val oauthUserSession = call.sessions.get<OauthUserSession>()

                when (session) {
                    null -> {
                        val user = userRepository.findOneById(oauthUserSession!!.userId, false, call)

                        when (user?.twoFaEnabled) {
                            true ->
                                call.respondMustache(
                                    templateCustomizerFactory,
                                    "otp-check.hbs",
                                    mapOf<String, Any>(),
                                )
                            else -> {
                                val session = UserTwoFaSession(user?.id.toString(), user!!.email)
                                call.sessions.set(session)
                            }
                        }
                    }
                    else -> {
                        when (session.userId == oauthUserSession!!.userId) {
                            true -> call.respondRedirect("/oauth/login")
                            else ->
                                call.respondMustache(
                                    templateCustomizerFactory,
                                    "otp-check.hbs",
                                    mapOf<String, Any>(),
                                )
                        }
                    }
                }
            }
        }

        post("/oauth/otp-check") {
            userSessionCheck(call) {
                val params = call.receiveParameters()

                val code = params["otp"] ?: "1"
                val saveDevice = params["save_device"]?.toBoolean() ?: false

                when (val result = twoFaService.verify(code, saveDevice, call)) {
                    is CallResult.Failure ->
                        call.respondMustache(
                            templateCustomizerFactory,
                            "otp-check.hbs",
                            mapOf<String, Any>(),
                        )
                    is CallResult.Success -> oauthLoginOptionService.completeLogin(call)
                }
            }
        }
    }

    log.info("[otpCheckRoute] -> Routes configured.")
}

suspend fun handleFailureResponse(
    call: ApplicationCall,
    result: CallResult.Failure<*, TwoFaServiceError>,
) {
    when (result.errorCode) {
        TwoFaServiceError.USER_NOT_FOUND -> call.respond(status = HttpStatusCode.NotFound, message = result)
        TwoFaServiceError.TWO_FA_ALREADY_ENABLED -> call.respond(status = HttpStatusCode.BadRequest, message = result)
        TwoFaServiceError.TWO_FA_NOT_ENABLED -> call.respond(status = HttpStatusCode.BadRequest, message = result)
        TwoFaServiceError.INVALID_CODE_OR_SECRET -> call.respond(status = HttpStatusCode.BadRequest, message = result)
        TwoFaServiceError.NO_LOGIN -> call.respondRedirect("/oauth/login")
        TwoFaServiceError.DEVICE_NOT_FOUND -> call.respond(status = HttpStatusCode.NotFound, message = result)
    }
}
