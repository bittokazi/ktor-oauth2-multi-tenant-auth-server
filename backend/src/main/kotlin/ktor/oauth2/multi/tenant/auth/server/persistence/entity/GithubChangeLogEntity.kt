package ktor.oauth2.multi.tenant.auth.server.persistence.entity

/**
 * Entity representing a formatted GitHub release changelog where the body is split into lines.
 */
data class GithubChangeLogEntity(
    val body: List<String>,
)
