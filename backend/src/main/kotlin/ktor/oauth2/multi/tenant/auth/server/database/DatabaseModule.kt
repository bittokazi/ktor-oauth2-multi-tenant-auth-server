package ktor.oauth2.multi.tenant.auth.server.database

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import ktor.oauth2.multi.tenant.auth.server.database.config.DatabaseConfigurationHolder
import ktor.oauth2.multi.tenant.auth.server.database.config.DefaultMultiTenantDatabaseConfiguration
import ktor.oauth2.multi.tenant.auth.server.database.config.MultiTenantDatabaseConfiguration

fun Application.configureDatabase() {
    dependencies {
        provide(DatabaseConfigurationHolder::class)
        provide<MultiTenantDatabaseConfiguration>(DefaultMultiTenantDatabaseConfiguration::class)
    }

    val multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration by dependencies

    multiTenantDatabaseConfiguration.init()
}
