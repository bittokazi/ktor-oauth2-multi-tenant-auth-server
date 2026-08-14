package ktor.oauth2.multi.tenant.auth.server.security.config

import io.ktor.server.plugins.di.annotations.Property
import kotlinx.serialization.Serializable

@Serializable
data class OauthConfig(
    @Property("oauth.enabled") val enabled: Boolean,
    @Property("oauth.issuer") val issuer: String,
    @Property("oauth.no-redirect-clients") val noRedirectClients: List<String> = listOf(),
    @Property("app.name") val appName: String,
    @Property("app.template-folder") val templateFolder: String,
    @Property("oauth.enable-issuer-provider") val enableIssuerProvider: Boolean = false,
)
