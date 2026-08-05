package ktor.oauth2.multi.tenant.auth.server.security.config

import io.ktor.server.plugins.di.annotations.Property

data class ExtraTokenClaimConfig(
    @Property("extra-token-claims") val extraTokenClaims: List<ExtraTokenClaim> = listOf(),
)

data class ExtraTokenClaim(
    val clientId: String,
    val claimName: String,
    val type: ExtraTokenClaimType,
    val claimValue: String,
)

enum class ExtraTokenClaimType {
    STRING,
    LIST,
}
