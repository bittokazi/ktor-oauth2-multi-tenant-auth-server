package ktor.oauth2.multi.tenant.auth.server.security.entity

import kotlinx.serialization.Serializable

@Serializable
data class UserTwoFaSession(
    val userId: String,
    val username: String,
)
