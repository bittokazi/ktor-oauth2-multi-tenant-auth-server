package ktor.oauth2.multi.tenant.auth.server.health.config

import io.ktor.server.plugins.di.annotations.Property
import kotlinx.serialization.Serializable

@Serializable
data class HealthConfiguration(
    @Property("app.health-endpoint.enabled") val enabled: Boolean,
)
