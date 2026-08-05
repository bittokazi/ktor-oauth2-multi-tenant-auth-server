package ktor.oauth2.multi.tenant.auth.server.app.user.controllers

import io.ktor.client.request.request
import io.ktor.http.*
import io.ktor.openapi.jsonSchema
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi
import ktor.oauth2.multi.tenant.auth.server.app.user.services.UserService
import ktor.oauth2.multi.tenant.auth.server.app.user.services.UserServiceError
import ktor.oauth2.multi.tenant.auth.server.app.user.services.UserServiceError.*
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_USER
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_USER_BY_ID
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_USER_SEARCH
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_USER_UPDATE_PASSWORD
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_USER_WHO_AM_I
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_USER_WHO_AM_I_UPDATE_PASSWORD
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PaginationRequest
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.ScopeEnum
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.UserData
import ktor.oauth2.multi.tenant.auth.server.security.services.scopeCheck
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@OptIn(ExperimentalKtorApi::class)
fun Application.userRoute() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val userService: UserService by dependencies

    routing {
        authenticate {
            get(API_USER_WHO_AM_I) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_WHO_AM_I,
                    ),
                    call,
                ) {
                    when (val CallResult = userService.whoAmI(call)) {
                        is CallResult.Success ->
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = CallResult.outcome,
                            )

                        is CallResult.Failure -> {
                            handleErrorResponse(CallResult)
                        }
                    }
                }
            }.describe {
                summary = "User Info"
                description = "Retrieves information about the current user."
                tag("User")
                security {
                    oauth2(ScopeEnum.USER_WHO_AM_I.name)
                }
                requestBody {
                    schema = jsonSchema<UserData>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "User information retrieved successfully"
                        schema = jsonSchema<UserData>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "Unauthorized to access user data"
                    }
                }
            }

            get(API_USER) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_READ_ALL,
                    ),
                    call,
                ) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message =
                            userService.getAll(
                                paginationRequest =
                                    PaginationRequest(
                                        page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                                        count = call.request.queryParameters["count"]?.toIntOrNull() ?: 10,
                                        query = call.request.queryParameters["query"] ?: "",
                                    ),
                                call = call,
                            ),
                    )
                }
            }.describe {
                summary = "Get Users"
                description = "Retrieves a list of users."
                tag("User")
                security {
                    oauth2(ScopeEnum.USER_READ_ALL.name)
                }
                parameters {
                    query(name = "page") {
                        description = "Page number for pagination, starts from 1"
                        required = false
                    }
                    query(name = "count") {
                        description = "Number of users to retrieve per page"
                        required = false
                    }
                    query(name = "query") {
                        description = "Search query for filtering users"
                        required = false
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Users retrieved successfully"
                        schema = jsonSchema<List<UserData>>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "Unauthorized to access user data"
                    }
                }
            }

            get(API_USER_BY_ID) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_READ,
                    ),
                    call,
                ) {
                    when (val CallResult = userService.get(call)) {
                        is CallResult.Success ->
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = CallResult.outcome,
                            )

                        is CallResult.Failure -> {
                            handleErrorResponse(CallResult)
                        }
                    }
                }
            }.describe {
                summary = "Get User"
                description = "Retrieves information about the current user."
                tag("User")
                security {
                    oauth2(ScopeEnum.USER_READ.name)
                }
                responses {
                    HttpStatusCode.OK {
                        description = "User information retrieved successfully"
                        schema = jsonSchema<UserData>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "Unauthorized to access user data"
                    }
                    HttpStatusCode.NotFound {
                        description = "User not found"
                    }
                }
            }

            get(API_USER_SEARCH) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_READ,
                    ),
                    call,
                ) {
                    when (val CallResult = userService.findByProperty(call)) {
                        is CallResult.Success ->
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = CallResult.outcome,
                            )

                        is CallResult.Failure -> {
                            handleErrorResponse(CallResult)
                        }
                    }
                }
            }.describe {
                summary = "Search Users"
                description = "Searches for users based on specified criteria."
                tag("User")
                security {
                    oauth2(ScopeEnum.USER_READ.name)
                }
                parameters {
                    query(name = "email") {
                        description = "Email address"
                        required = true
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Users found successfully"
                        schema = jsonSchema<UserData>()
                    }
                    HttpStatusCode.NotFound {
                        description = "Users not found"
                    }
                }
            }

            post(API_USER) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_ADD,
                    ),
                    call,
                ) {
                    val payload = call.receive<UserData>()

                    when (val CallResult = userService.register(call, payload, false)) {
                        is CallResult.Success ->
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = CallResult.outcome,
                            )

                        is CallResult.Failure -> handleErrorResponse(CallResult)
                    }
                }
            }.describe {
                summary = "Add User"
                description = "Adds a new user."
                tag("User")
                security {
                    oauth2(ScopeEnum.USER_ADD.name)
                }
                requestBody {
                    schema = jsonSchema<UserData>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "User added successfully"
                        schema = jsonSchema<UserData>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "Invalid user data"
                    }
                }
            }

            put(API_USER_BY_ID) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_UPDATE,
                    ),
                    call,
                ) {
                    val payload = call.receive<UserData>()

                    when (val CallResult = userService.update(call, payload, false)) {
                        is CallResult.Success ->
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = CallResult.outcome,
                            )

                        is CallResult.Failure -> {
                            handleErrorResponse(CallResult)
                        }
                    }
                }
            }.describe {
                summary = "Update User"
                description = "Updates an existing user."
                tag("User")
                security {
                    oauth2(ScopeEnum.USER_UPDATE.name)
                }
                requestBody {
                    schema = jsonSchema<UserData>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "User updated successfully"
                        schema = jsonSchema<UserData>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "Invalid user data"
                    }
                }
            }

            put(API_USER_UPDATE_PASSWORD) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_UPDATE,
                    ),
                    call,
                ) {
                    val payload = call.receive<UserData>()

                    when (val CallResult = userService.updatePassword(call, payload, false)) {
                        is CallResult.Success ->
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = CallResult.outcome,
                            )

                        is CallResult.Failure -> {
                            handleErrorResponse(CallResult)
                        }
                    }
                }
            }.describe {
                summary = "Update User Password"
                description = "Updates the password for an existing user."
                tag("User")
                security {
                    oauth2(ScopeEnum.USER_UPDATE.name)
                }
                requestBody {
                    schema = jsonSchema<UserData>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "User password updated successfully"
                        schema = jsonSchema<UserData>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "Invalid user data"
                    }
                }
            }

            put(API_USER_WHO_AM_I) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_WHO_AM_I,
                    ),
                    call,
                ) {
                    val payload = call.receive<UserData>()

                    when (val CallResult = userService.update(call, payload, true)) {
                        is CallResult.Success ->
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = CallResult.outcome,
                            )

                        is CallResult.Failure -> {
                            handleErrorResponse(CallResult)
                        }
                    }
                }
            }.describe {
                summary = "Update Current User"
                description = "Updates current user."
                tag("User")
                security {
                    oauth2(ScopeEnum.USER_WHO_AM_I.name)
                }
                requestBody {
                    schema = jsonSchema<UserData>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "User updated successfully"
                        schema = jsonSchema<UserData>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "Invalid user data"
                    }
                }
            }

            put(API_USER_WHO_AM_I_UPDATE_PASSWORD) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_WHO_AM_I,
                    ),
                    call,
                ) {
                    val payload = call.receive<UserData>()

                    when (val CallResult = userService.updatePassword(call, payload, true)) {
                        is CallResult.Success ->
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = CallResult.outcome,
                            )

                        is CallResult.Failure -> {
                            handleErrorResponse(CallResult)
                        }
                    }
                }
            }.describe {
                summary = "Update Current User Password"
                description = "Updates the password for current user."
                tag("User")
                security {
                    oauth2(ScopeEnum.USER_WHO_AM_I.name)
                }
                requestBody {
                    schema = jsonSchema<UserData>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "User password updated successfully"
                        schema = jsonSchema<UserData>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "Invalid user data"
                    }
                }
            }
        }
    }

    log.info("[userRoute] -> Routes configured.")
}

private suspend fun RoutingContext.handleErrorResponse(callResult: CallResult.Failure<UserData, UserServiceError>) {
    when (callResult.errorCode) {
        NOT_FOUND ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message = callResult,
            )

        UNPROCESSABLE_ENTITY ->
            call.respond(
                status = HttpStatusCode.UnprocessableEntity,
                message = callResult,
            )

        EMAIL_EXIST ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message =
                    mapOf(
                        "email" to listOf("EMAIL_EXIST"),
                    ),
            )

        PASSWORD_DOES_NOT_MATCH ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message =
                    mapOf(
                        "currentPassword" to listOf("PASSWORD_DOES_NOT_MATCH"),
                    ),
            )
    }
}
