package ktor.oauth2.multi.tenant.auth.server.persistence.repository

import io.ktor.server.application.ApplicationCall
import ktor.oauth2.multi.tenant.auth.server.database.config.MultiTenantDatabaseConfiguration
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.User
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.UserTwoFaSecret
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.UserTwoFaSecretData
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.UserTwoFaSecrets
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class UserTwoFaSecretRepository(
    multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : BaseRepository(multiTenantDatabaseConfiguration) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[UserTwoFaSecretRepository] -> created")
    }

    fun findByUserId(
        userId: String,
        call: ApplicationCall,
    ): UserTwoFaSecretData? {
        return query(call = call) {
            val user = User.findById(userId) ?: return@query null
            UserTwoFaSecret.find { UserTwoFaSecrets.user eq user.id }.singleOrNull()?.toDto()
        }
    }

    fun insert(
        userTwoFaSecret: UserTwoFaSecretData,
        call: ApplicationCall,
    ): UserTwoFaSecretData {
        return query(call = call) {
            UserTwoFaSecret.new {
                user = userTwoFaSecret.user.let {
                    User.findById(it.id!!)
                } ?: throw IllegalArgumentException("User must be provided for UserTwoFaSecret")

                secret = userTwoFaSecret.secret
                scratchCodes = userTwoFaSecret.scratchCodes
            }.toDto()
        }
    }

    fun updateScratchCodes(
        userTwoFaSecretData: UserTwoFaSecretData,
        call: ApplicationCall,
    ): UserTwoFaSecretData? {
        return query(call = call) {
            UserTwoFaSecret.findByIdAndUpdate(userTwoFaSecretData.id!!) { secret ->
                secret.scratchCodes = userTwoFaSecretData.scratchCodes
            }?.toDto()
        }
    }

    fun delete(
        userId: String,
        call: ApplicationCall,
    ) {
        query(call = call) {
            UserTwoFaSecrets.deleteWhere { UserTwoFaSecrets.user eq userId }
        }
    }
}
