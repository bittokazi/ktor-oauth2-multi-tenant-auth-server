package ktor.oauth2.multi.tenant.auth.server.security.config

import io.ktor.server.plugins.di.annotations.Property
import kotlinx.serialization.Serializable

@Serializable
data class ExtraTokenClaimConfig(
    @Property("extra-token-claims") val extraTokenClaims: List<ExtraTokenClaim> = listOf(),
)

@Serializable
data class ExtraTokenClaim(
    val clientId: String,
    val claimName: String,
    val type: ExtraTokenClaimType,
    val claimValue: String,
)

@Serializable
enum class ExtraTokenClaimType {
    STRING,
    LIST,
}
