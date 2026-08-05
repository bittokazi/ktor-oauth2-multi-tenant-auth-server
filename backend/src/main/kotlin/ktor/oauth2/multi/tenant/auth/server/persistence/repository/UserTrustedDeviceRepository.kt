package ktor.oauth2.multi.tenant.auth.server.persistence.repository

import io.ktor.server.application.ApplicationCall
import ktor.oauth2.multi.tenant.auth.server.database.config.MultiTenantDatabaseConfiguration
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class UserTrustedDeviceRepository(
    multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : BaseRepository(multiTenantDatabaseConfiguration) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[UserTrustedDeviceRepository] -> created")
    }

    fun findAllByUserId(
        userId: String,
        call: ApplicationCall,
    ): List<UserTrustedDeviceData> {
        return query(call = call) {
            UserTrustedDevice.find { UserTrustedDevices.user eq userId }.map { it.toDto() }
        }
    }

    fun findByUserIdAndId(
        userId: String,
        id: Long,
        call: ApplicationCall,
    ): UserTrustedDeviceData? {
        return query(call = call) {
            UserTrustedDevice.find {
                (UserTrustedDevices.user eq userId) and
                    (UserTrustedDevices.id eq id)
            }.singleOrNull()?.toDto()
        }
    }

    fun findAllByUserIdAndInstanceId(
        userId: String,
        instanceId: String,
        call: ApplicationCall,
    ): UserTrustedDeviceData? {
        return query(call = call) {
            UserTrustedDevice.find {
                (UserTrustedDevices.user eq userId) and
                    (UserTrustedDevices.instanceId eq instanceId)
            }.singleOrNull()?.toDto()
        }
    }

    fun deleteAllByUserIdAndInstanceId(
        userId: String,
        instanceId: String,
        call: ApplicationCall,
    ) {
        return query(call = call) {
            UserTrustedDevices.deleteWhere {
                (UserTrustedDevices.user eq userId) and
                    (UserTrustedDevices.instanceId eq instanceId)
            }
        }
    }

    fun deleteByUserIdAndId(
        userId: String,
        id: Long,
        call: ApplicationCall,
    ) {
        return query(call = call) {
            UserTrustedDevices.deleteWhere {
                (UserTrustedDevices.user eq userId) and
                    (UserTrustedDevices.id eq id)
            }
        }
    }

    fun deleteAllByUserId(
        userId: String,
        call: ApplicationCall,
    ) {
        return query(call = call) {
            UserTrustedDevices.deleteWhere { UserTrustedDevices.user eq userId }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun insert(
        userId: String,
        instanceId: String,
        deviceIp: String?,
        userAgent: String?,
        call: ApplicationCall,
    ): UserTrustedDeviceData {
        return query(call = call) {
            UserTrustedDevice.new {
                user = User.findById(userId)!!
                this.instanceId = instanceId
                this.deviceIp = deviceIp
                this.userAgent = userAgent
                this.createdDate = Clock.System.now()
            }.toDto()
        }
    }
}
