package ktor.oauth2.multi.tenant.auth.server.security.entity

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)
