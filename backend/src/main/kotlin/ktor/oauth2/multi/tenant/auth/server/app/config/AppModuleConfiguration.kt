package ktor.oauth2.multi.tenant.auth.server.app.config

import io.ktor.server.plugins.di.annotations.Property
import kotlinx.serialization.Serializable

@Serializable
data class AppModuleConfiguration(
    @Property("app.modules") val modules: Map<AppModule, AppModuleConfig>,
    @Property("app.domain") val domain: String = "",
    @Property("app.template-folder") val templateFolder: String,
    @Property("app.name") val appName: String,
)

@Serializable
enum class AppModule {
    USER,
    CPANEL,
    CLIENT,
    PASSWORD_RESET,
    ROLE,
    TENANT,
}

@Serializable
data class AppModuleConfig(
    val enabled: Boolean,
)

@Serializable
data class AppInfo(
    val version: String,
)
