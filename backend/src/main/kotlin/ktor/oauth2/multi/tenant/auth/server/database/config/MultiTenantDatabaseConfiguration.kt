package ktor.oauth2.multi.tenant.auth.server.database.config

import org.jetbrains.exposed.v1.jdbc.Database

interface MultiTenantDatabaseConfiguration {
    fun init()

    fun getTenantDatabase(
        key: String,
        schema: String,
    ): Database

    fun createDatabaseSchema(schema: String)
}
