package ktor.oauth2.multi.tenant.auth.server.persistence.entity

import kotlinx.serialization.Serializable

@Serializable
data class GitHubChangeLogDto(
    val body: String? = null,
)
