package ktor.oauth2.multi.tenant.auth.server.security.database

import com.bittokazi.ktor.auth.database.OauthDatabaseConfiguration
import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey
import ktor.oauth2.multi.tenant.auth.server.config.AppConfig.Companion.AUTH_TENANT_ATTRIBUTE_KEY
import ktor.oauth2.multi.tenant.auth.server.database.config.MultiTenantDatabaseConfiguration
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OauthDatabaseConfigurationImpl(
    val multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : OauthDatabaseConfiguration {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[OauthDatabaseConfiguration] -> init")
    }

    override fun <T> dbQuery(
        call: ApplicationCall?,
        block: () -> T,
    ): T {
        return transaction(
            multiTenantDatabaseConfiguration.getTenantDatabase(
                "default",
                call!!.attributes[AttributeKey<String>(AUTH_TENANT_ATTRIBUTE_KEY)],
            ),
        ) { block() }
    }
}
