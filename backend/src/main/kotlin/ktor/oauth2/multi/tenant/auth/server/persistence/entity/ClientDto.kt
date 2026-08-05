package ktor.oauth2.multi.tenant.auth.server.persistence.entity

import kotlinx.serialization.Serializable

@Serializable
data class ClientDto(
    val id: String?,
    var clientId: String? = null,
    var clientName: String,
    var clientType: String? = "confidential",
    var redirectUris: List<String>,
    var scopes: List<String>,
    var grantTypes: List<String>,
    var clientSecret: String? = null,
    var accessTokenValidity: Long = 300,
    var refreshTokenValidity: Long = 7200,
    var isDefault: Boolean = false,
    var consentRequired: Boolean = true,
    var newSecret: String? = null,
)
