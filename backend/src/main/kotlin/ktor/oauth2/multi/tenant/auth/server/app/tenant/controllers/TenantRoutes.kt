package ktor.oauth2.multi.tenant.auth.server.app.tenant.controllers

import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import io.ktor.utils.io.ExperimentalKtorApi
import ktor.oauth2.multi.tenant.auth.server.app.tenant.services.TenantService
import ktor.oauth2.multi.tenant.auth.server.app.tenant.services.TenantServiceError
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_TENANT
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_TENANT_BY_ID
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_TENANT_INFO
import ktor.oauth2.multi.tenant.auth.server.config.RouteConfig.API_TENANT_TEMPLATE_UPLOAD
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.ScopeEnum
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.TenantData
import ktor.oauth2.multi.tenant.auth.server.security.services.scopeCheck

@OptIn(ExperimentalKtorApi::class)
fun Application.tenantRoute() {
    val tenantService: TenantService by dependencies

    routing {
        get(API_TENANT_INFO) {
            when (val result = tenantService.getTenantInfo(call)) {
                is CallResult.Success -> call.respond(result.outcome)

                is CallResult.Failure -> handleErrorResponse(result)
            }
        }.describe {
            summary = "Tenant Information"
            description = "Retrieves information about the current tenant."
            tag("Tenant")
            responses {
                HttpStatusCode.OK {
                    description = "Tenant information retrieved successfully"
                    schema = jsonSchema<TenantData>()
                }
                HttpStatusCode.NotFound {
                    description = "Tenant not found"
                }
            }
        }

        authenticate {
            get(API_TENANT) {
                scopeCheck(listOf(ScopeEnum.TENANT_ALL), call) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = tenantService.getAll(call),
                    )
                }
            }.describe {
                summary = "Get All Tenants"
                description = "Retrieves information about all tenants."
                tag("Tenant")
                security {
                    oauth2(ScopeEnum.TENANT_ALL.name)
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Tenant information retrieved successfully"
                        schema = jsonSchema<List<TenantData>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "Tenant not found"
                    }
                }
            }

            post(API_TENANT) {
                scopeCheck(listOf(ScopeEnum.TENANT_ALL), call) {
                    val payload = call.receive<TenantData>()

                    when (val callResult = tenantService.add(payload, call)) {
                        is CallResult.Success -> call.respond(status = HttpStatusCode.Created, message = callResult.outcome)
                        is CallResult.Failure -> handleErrorResponse(callResult)
                    }
                }
            }.describe {
                summary = "Add Tenant"
                description = "Adds a new tenant."
                tag("Tenant")
                security {
                    oauth2(ScopeEnum.TENANT_ALL.name)
                }
                requestBody {
                    schema = jsonSchema<TenantData>()
                }
                responses {
                    HttpStatusCode.Created {
                        description = "Tenant added successfully"
                        schema = jsonSchema<TenantData>()
                    }
                    HttpStatusCode.NotFound {
                        description = "Tenant not found"
                    }
                }
            }

            get(API_TENANT_BY_ID) {
                scopeCheck(listOf(ScopeEnum.TENANT_ALL), call) {
                    when (val callResult = tenantService.get(call)) {
                        is CallResult.Success -> call.respond(status = HttpStatusCode.OK, message = callResult.outcome)
                        is CallResult.Failure -> handleErrorResponse(callResult)
                    }
                }
            }.describe {
                summary = "Get Tenant"
                description = "Retrieves information about a specific tenant."
                tag("Tenant")
                security {
                    oauth2(ScopeEnum.TENANT_ALL.name)
                }
                responses {
                    HttpStatusCode.Created {
                        description = "Tenant information retrieved successfully"
                        schema = jsonSchema<TenantData>()
                    }
                    HttpStatusCode.NotFound {
                        description = "Tenant not found"
                    }
                    HttpStatusCode.UnprocessableEntity {
                        description = "Unable to process the request"
                    }
                }
            }

            put(API_TENANT_BY_ID) {
                scopeCheck(listOf(ScopeEnum.TENANT_ALL), call) {
                    val payload = call.receive<TenantData>()

                    when (val callResult = tenantService.update(call, payload)) {
                        is CallResult.Success -> call.respond(status = HttpStatusCode.OK, message = callResult.outcome)
                        is CallResult.Failure -> handleErrorResponse(callResult)
                    }
                }
            }.describe {
                summary = "Update Tenant"
                description = "Updates information for an existing tenant."
                tag("Tenant")
                security {
                    oauth2(ScopeEnum.TENANT_ALL.name)
                }
                requestBody {
                    schema = jsonSchema<TenantData>()
                }
                responses {
                    HttpStatusCode.Created {
                        description = "Tenant updated successfully"
                        schema = jsonSchema<TenantData>()
                    }
                    HttpStatusCode.NotFound {
                        description = "Tenant not found"
                    }
                    HttpStatusCode.UnprocessableEntity {
                        description = "Unable to process the request"
                    }
                }
            }

            post(API_TENANT_TEMPLATE_UPLOAD) {
                scopeCheck(listOf(ScopeEnum.TENANT_ALL), call) {
                    tenantService.uploadTemplate(call) { result ->
                        when (result) {
                            is CallResult.Success -> call.respond(status = HttpStatusCode.OK, message = result.outcome)
                            is CallResult.Failure -> call.respond(status = HttpStatusCode.UnprocessableEntity, message = result)
                        }
                    }
                }
            }.describe {
                summary = "Upload Tenant Template"
                description = "Uploads a template for a new tenant."
                tag("Tenant")
                security {
                    oauth2(ScopeEnum.TENANT_ALL.name)
                }
                requestBody {
                    schema = jsonSchema<TenantData>()
                }
                responses {
                    HttpStatusCode.Created {
                        description = "Tenant template uploaded successfully"
                        schema = jsonSchema<Map<String, String>>()
                    }
                    HttpStatusCode.UnprocessableEntity {
                        description = "Unable to process the request"
                    }
                }
            }
        }
    }

    log.info("[tenantRoute] -> Routes configured.")
}

private suspend fun RoutingContext.handleErrorResponse(callResult: CallResult.Failure<*, TenantServiceError>) {
    when (callResult.errorCode) {
        TenantServiceError.NOT_FOUND ->
            call.respond(status = HttpStatusCode.NotFound, message = callResult)

        TenantServiceError.UNPROCESSABLE_ENTITY ->
            call.respond(status = HttpStatusCode.UnprocessableEntity, message = callResult)
    }
}
