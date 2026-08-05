package ktor.oauth2.multi.tenant.auth.server.database.config

import io.ktor.server.plugins.di.annotations.Property
import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfigurationHolder(
    @Property("databases") val databases: Map<String, DatabaseConfiguration>,
)

@Serializable
data class DatabaseConfiguration(
    @Property("url") val url: String,
    @Property("username") val username: String,
    @Property("password") val password: String,
    @Property("driver") val driver: String,
    @Property("schema") val schema: String,
)
