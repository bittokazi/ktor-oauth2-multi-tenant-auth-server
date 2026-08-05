package ktor.oauth2.multi.tenant.auth.server.app.role.controllers

import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi
import ktor.oauth2.multi.tenant.auth.server.app.role.services.RoleService
import ktor.oauth2.multi.tenant.auth.server.app.role.services.RoleServiceError
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_ROLE
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_ROLE_BY_ID
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.RoleDto
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.ScopeEnum
import ktor.oauth2.multi.tenant.auth.server.security.services.scopeCheck
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@OptIn(ExperimentalKtorApi::class)
fun Application.roleRoutes() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val roleService: RoleService by dependencies

    routing {
        authenticate {
            get(API_ROLE) {
                scopeCheck(
                    listOf(
                        ScopeEnum.ROLE_READ_ALL,
                    ),
                    call,
                ) {
                    call.respond(roleService.findAll(call))
                }
            }.describe {
                summary = "Get All Roles"
                description = "Retrieves information about all roles."
                tag("Role")
                security {
                    oauth2(ScopeEnum.ROLE_READ_ALL.name)
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Roles retrieved successfully"
                        schema = jsonSchema<List<RoleDto>>()
                    }
                }
            }

            post(API_ROLE) {
                scopeCheck(
                    listOf(
                        ScopeEnum.ROLE_READ_ALL,
                    ),
                    call,
                ) {
                    val payload = call.receive<RoleDto>()

                    when (val callResult = roleService.create(payload, call)) {
                        is CallResult.Success -> call.respond(status = HttpStatusCode.Created, message = callResult.outcome)
                        is CallResult.Failure -> handleErrorResponse(callResult)
                    }
                }
            }.describe {
                summary = "Add Role"
                description = "Adds a new role."
                tag("Role")
                security {
                    oauth2(ScopeEnum.ROLE_ADD.name)
                }
                requestBody {
                    schema = jsonSchema<RoleDto>()
                }
                responses {
                    HttpStatusCode.Created {
                        description = "Role added successfully"
                        schema = jsonSchema<RoleDto>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "Invalid request data"
                    }
                    HttpStatusCode.UnprocessableEntity {
                        description = "Unable to process the request"
                    }
                }
            }

            get(API_ROLE_BY_ID) {
                scopeCheck(
                    listOf(
                        ScopeEnum.ROLE_READ,
                    ),
                    call,
                ) {
                    val id =
                        call.parameters["id"] ?: return@scopeCheck call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = mapOf("error" to "missing_id"),
                        )

                    when (val callResult = roleService.get(id, call)) {
                        is CallResult.Success -> call.respond(status = HttpStatusCode.OK, message = callResult.outcome)
                        is CallResult.Failure -> handleErrorResponse(callResult)
                    }
                }
            }.describe {
                summary = "Get Role by ID"
                description = "Retrieves information about a specific role."
                tag("Role")
                security {
                    oauth2(ScopeEnum.ROLE_READ.name)
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Role retrieved successfully"
                        schema = jsonSchema<RoleDto>()
                    }
                    HttpStatusCode.NotFound {
                        description = "Role not found"
                    }
                }
            }

            put(API_ROLE_BY_ID) {
                scopeCheck(
                    listOf(
                        ScopeEnum.ROLE_UPDATE,
                    ),
                    call,
                ) {
                    val id =
                        call.parameters["id"] ?: return@scopeCheck call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = mapOf("error" to "missing_id"),
                        )

                    val payload = call.receive<RoleDto>()

                    when (val callResult = roleService.update(id, payload, call)) {
                        is CallResult.Success -> call.respond(status = HttpStatusCode.OK, message = callResult.outcome)
                        is CallResult.Failure -> handleErrorResponse(callResult)
                    }
                }
            }.describe {
                summary = "Update Role"
                description = "Updates an existing role."
                tag("Role")
                security {
                    oauth2(ScopeEnum.ROLE_UPDATE.name)
                }
                requestBody {
                    schema = jsonSchema<RoleDto>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Role updated successfully"
                        schema = jsonSchema<RoleDto>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "Invalid request data"
                    }
                    HttpStatusCode.UnprocessableEntity {
                        description = "Unable to process the request"
                    }
                }
            }
        }
    }

    log.info("[roleRoute] -> Routes configured.")
}

private suspend fun RoutingContext.handleErrorResponse(callResult: CallResult.Failure<*, RoleServiceError>) {
    when (callResult.errorCode) {
        RoleServiceError.NOT_FOUND ->
            call.respond(status = HttpStatusCode.NotFound, message = callResult)

        RoleServiceError.ROLE_KEY_EXIST ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = callResult.errorData ?: mapOf("error" to listOf("Role key already exists")),
            )

        RoleServiceError.UNPROCESSABLE_ENTITY ->
            call.respond(status = HttpStatusCode.UnprocessableEntity, message = callResult)
    }
}
