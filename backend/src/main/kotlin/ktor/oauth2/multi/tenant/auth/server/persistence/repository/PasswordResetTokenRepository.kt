package ktor.oauth2.multi.tenant.auth.server.persistence.repository

import io.ktor.server.application.ApplicationCall
import korlibs.time.hours
import ktor.oauth2.multi.tenant.auth.server.database.config.MultiTenantDatabaseConfiguration
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PasswordResetToken
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PasswordResetTokenData
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PasswordResetTokens
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PasswordResetTokens.expireDate
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PasswordResetTokens.token
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.User
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.UserData
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class)
class PasswordResetTokenRepository(
    multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : BaseRepository(multiTenantDatabaseConfiguration) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[PasswordResetTokenRepository] -> created")
    }

    @OptIn(ExperimentalUuidApi::class)
    fun create(
        userId: String,
        call: ApplicationCall,
    ): PasswordResetTokenData {
        return query(call = call) {
            PasswordResetToken.new {
                token = Uuid.random().toString().replace("-", "")
                user = User.findById(userId)!!
                expireDate = Clock.System.now() + 2.hours
            }.toDto()
        }
    }

    fun findByToken(
        token: String,
        call: ApplicationCall,
    ): PasswordResetTokenData? {
        return query(call = call) {
            PasswordResetToken.find { PasswordResetTokens.token eq token }.singleOrNull()?.toDto()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun delete(
        userData: UserData,
        call: ApplicationCall,
    ): Int {
        return query(call = call) {
            PasswordResetTokens.deleteWhere { PasswordResetTokens.user eq userData.id }
        }
    }
}
