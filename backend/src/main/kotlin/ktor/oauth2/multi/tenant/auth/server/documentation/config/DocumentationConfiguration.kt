package ktor.oauth2.multi.tenant.auth.server.documentation.config

import io.ktor.server.plugins.di.annotations.Property
import kotlinx.serialization.Serializable

@Serializable
data class DocumentationConfiguration(
    @Property("app.documentation.enabled") val enabled: Boolean,
)
