package ktor.oauth2.multi.tenant.auth.server.app.client.controllers

import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import io.ktor.utils.io.ExperimentalKtorApi
import ktor.oauth2.multi.tenant.auth.server.app.client.services.ClientService
import ktor.oauth2.multi.tenant.auth.server.app.client.services.ClientServiceError
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_CLIENT
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_CLIENT_BY_ID
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.ClientDto
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.ScopeEnum
import ktor.oauth2.multi.tenant.auth.server.security.services.scopeCheck
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@OptIn(ExperimentalKtorApi::class)
fun Application.clientRoutes() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val clientService: ClientService by dependencies

    routing {
        authenticate {
            get(API_CLIENT) {
                scopeCheck(
                    listOf(
                        ScopeEnum.ROLE_SUPER_ADMIN,
                        ScopeEnum.CLIENT_READ_ALL,
                    ),
                    call,
                ) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = clientService.all(call),
                    )
                }
            }.describe {
                summary = "Get All Clients"
                description = "Retrieves information about all clients."
                tag("Client")
                security {
                    oauth2(ScopeEnum.ROLE_SUPER_ADMIN.name, ScopeEnum.CLIENT_READ_ALL.name)
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Clients retrieved successfully"
                        schema = jsonSchema<List<ClientDto>>()
                    }
                }
            }

            post(API_CLIENT) {
                scopeCheck(
                    listOf(
                        ScopeEnum.ROLE_SUPER_ADMIN,
                        ScopeEnum.CLIENT_ADD,
                    ),
                    call,
                ) {
                    val payload = call.receive<ClientDto>()

                    when (val result = clientService.save(payload, call)) {
                        is CallResult.Success -> call.respond(status = HttpStatusCode.OK, message = result.outcome)
                        is CallResult.Failure -> handleFailureResponse(call, result)
                    }
                }
            }.describe {
                summary = "Add Client"
                description = "Adds a new client."
                tag("Client")
                security {
                    oauth2(ScopeEnum.ROLE_SUPER_ADMIN.name, ScopeEnum.CLIENT_ADD.name)
                }
                requestBody {
                    schema = jsonSchema<ClientDto>()
                }
                responses {
                    HttpStatusCode.Created {
                        description = "Client added successfully"
                        schema = jsonSchema<ClientDto>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "Invalid request data"
                    }
                    HttpStatusCode.UnprocessableEntity {
                        description = "Unable to process the request"
                    }
                }
            }

            get(API_CLIENT_BY_ID) {
                scopeCheck(
                    listOf(
                        ScopeEnum.ROLE_SUPER_ADMIN,
                        ScopeEnum.CLIENT_READ,
                    ),
                    call,
                ) {
                    call.parameters["id"]?.let {
                        when (val result = clientService.get(it, call)) {
                            is CallResult.Success -> call.respond(status = HttpStatusCode.OK, message = result.outcome)
                            is CallResult.Failure -> handleFailureResponse(call, result)
                        }
                    }
                }
            }.describe {
                summary = "Get Client by ID"
                description = "Retrieves information about a specific client."
                tag("Client")
                security {
                    oauth2(ScopeEnum.ROLE_SUPER_ADMIN.name, ScopeEnum.CLIENT_READ.name)
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Client retrieved successfully"
                        schema = jsonSchema<ClientDto>()
                    }
                    HttpStatusCode.NotFound {
                        description = "Client not found"
                    }
                }
            }

            put(API_CLIENT_BY_ID) {
                scopeCheck(
                    listOf(
                        ScopeEnum.ROLE_SUPER_ADMIN,
                        ScopeEnum.CLIENT_UPDATE,
                    ),
                    call,
                ) {
                    call.parameters["id"]?.let {
                        val payload = call.receive<ClientDto>()

                        when (
                            val result =
                                clientService.update(
                                    payload,
                                    call.queryParameters["newSecret"]?.toBoolean() ?: false,
                                    call,
                                )
                        ) {
                            is CallResult.Success -> call.respond(status = HttpStatusCode.OK, message = result.outcome)
                            is CallResult.Failure -> handleFailureResponse(call, result)
                        }
                    }
                }
            }.describe {
                summary = "Update Client"
                description = "Updates an existing client."
                tag("Client")
                security {
                    oauth2(ScopeEnum.ROLE_SUPER_ADMIN.name, ScopeEnum.CLIENT_UPDATE.name)
                }
                requestBody {
                    schema = jsonSchema<ClientDto>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Client updated successfully"
                        schema = jsonSchema<ClientDto>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "Invalid request data"
                    }
                    HttpStatusCode.UnprocessableEntity {
                        description = "Unable to process the request"
                    }
                }
            }

            delete(API_CLIENT_BY_ID) {
                scopeCheck(
                    listOf(
                        ScopeEnum.ROLE_SUPER_ADMIN,
                        ScopeEnum.CLIENT_REMOVE,
                    ),
                    call,
                ) {
                    call.parameters["id"]?.let {
                        when (val result = clientService.delete(it, call)) {
                            is CallResult.Success -> call.respond(status = HttpStatusCode.OK, message = result.outcome)
                            is CallResult.Failure -> handleFailureResponse(call, result)
                        }
                    }
                }
            }.describe {
                summary = "Delete Client by ID"
                description = "Deletes a specific client."
                tag("Client")
                security {
                    oauth2(ScopeEnum.ROLE_SUPER_ADMIN.name, ScopeEnum.CLIENT_REMOVE.name)
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Client deleted successfully"
                        schema = jsonSchema<ClientDto>()
                    }
                    HttpStatusCode.NotFound {
                        description = "Client not found"
                    }
                }
            }
        }
    }

    log.info("[clientRoutes] -> Routes configured.")
}

suspend fun handleFailureResponse(
    call: ApplicationCall,
    result: CallResult.Failure<ClientDto, ClientServiceError>,
) {
    when (result.errorCode) {
        ClientServiceError.CLIENT_EXIST -> call.respond(status = HttpStatusCode.BadRequest, message = result)
        ClientServiceError.INSERT_ERROR -> call.respond(status = HttpStatusCode.UnprocessableEntity, message = result)
        ClientServiceError.UPDATE_ERROR -> call.respond(status = HttpStatusCode.UnprocessableEntity, message = result)
        ClientServiceError.NOT_FOUND -> call.respond(status = HttpStatusCode.NotFound, message = result)
        ClientServiceError.DELETE_ERROR, ClientServiceError.DEFAULT_DELETE_NOT_PERMITTED ->
            call.respond(status = HttpStatusCode.UnprocessableEntity, message = result)
    }
}
