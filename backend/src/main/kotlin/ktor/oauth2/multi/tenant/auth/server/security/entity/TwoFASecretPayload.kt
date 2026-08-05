package ktor.oauth2.multi.tenant.auth.server.security.entity

import kotlinx.serialization.Serializable

@Serializable
data class TwoFASecretPayload(
    var tenantName: String? = null,
    var secret: String? = null,
    var code: Int? = null,
    var enabled: Boolean? = null,
    var scratchCodes: List<String> = ArrayList(),
)
