package ktor.oauth2.multi.tenant.auth.server.persistence.entity

data class PaginationRequest(
    val page: Int?,
    val count: Int?,
    val query: String?,
)
