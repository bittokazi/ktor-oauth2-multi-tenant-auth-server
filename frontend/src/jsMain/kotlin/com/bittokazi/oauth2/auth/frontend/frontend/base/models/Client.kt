package com.bittokazi.oauth2.auth.frontend.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class Client(
    val id: String? = null,
    var clientId: String,
    var clientName: String,
    var clientType: String = "confidential",
    var redirectUris: List<String>,
    var scopes: List<String>,
    var grantTypes: List<String>,
    var clientSecret: String? = null,
    var accessTokenValidity: Long = 300,
    var refreshTokenValidity: Long = 7200,
    var isDefault: Boolean = false,
    var consentRequired: Boolean = true,
    var newSecret: String? = null
)
