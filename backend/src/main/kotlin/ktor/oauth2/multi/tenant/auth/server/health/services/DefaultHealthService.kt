package ktor.oauth2.multi.tenant.auth.server.health.services

import io.ktor.server.application.*
import ktor.oauth2.multi.tenant.auth.server.database.config.DatabaseConfigurationHolder
import ktor.oauth2.multi.tenant.auth.server.database.config.MultiTenantDatabaseConfiguration
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DefaultHealthService(
    private val databaseConfigurationHolder: DatabaseConfigurationHolder,
    private val multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : HealthService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[DefaultHealthService] -> init")
    }

    override fun checkHealth(call: ApplicationCall): CallResult<HealthCheckResponse, HealthServiceErrorCode> {
        return try {
            // Get the first database configuration
            val (tenantKey, dbConfig) =
                databaseConfigurationHolder.databases.entries.firstOrNull()
                    ?: throw IllegalStateException("No database configuration found")

            val schema = dbConfig.schema

            // Check database connectivity by attempting a transaction
            val database = multiTenantDatabaseConfiguration.getTenantDatabase(key = tenantKey, schema = schema)

            // Execute a transaction which verifies the database connection is active
            transaction(database) {
                exec("SELECT 1") { rs ->
                    if (rs.next()) {
                        log.debug("[checkHealth] -> Query executed successfully")
                    } else {
                        throw Exception("Database connection check failed - no result from SELECT 1")
                    }
                }
            }

            log.debug("[checkHealth] -> Database connection active for tenant: $tenantKey, schema: $schema")
            CallResult.Success(
                HealthCheckResponse(
                    status = "UP",
                    message = "Application is healthy - Database connection active",
                ),
            )
        } catch (e: Exception) {
            handleHealthCheckError(e)
        }
    }

    private fun handleHealthCheckError(exception: Exception): CallResult<HealthCheckResponse, HealthServiceErrorCode> {
        log.error("[checkHealth] -> Health check failed: ${exception.message}", exception)
        return CallResult.Failure(HealthServiceErrorCode.HEALTH_CHECK_FAILED)
    }
}
