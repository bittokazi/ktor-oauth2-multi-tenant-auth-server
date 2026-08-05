package ktor.oauth2.multi.tenant.auth.server.utils

import io.ktor.server.application.ApplicationCall
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PaginationRequest
import kotlin.text.toInt

suspend fun paginatedRequestFilter(
    call: ApplicationCall,
    function: suspend (PaginationRequest) -> Unit,
) {
    val paginationRequest =
        PaginationRequest(
            page = call.request.queryParameters["page"]?.toInt() ?: 1,
            count = call.request.queryParameters["count"]?.toInt() ?: 1,
            query = call.request.queryParameters["query"] ?: "",
        )

    function(paginationRequest)
}
