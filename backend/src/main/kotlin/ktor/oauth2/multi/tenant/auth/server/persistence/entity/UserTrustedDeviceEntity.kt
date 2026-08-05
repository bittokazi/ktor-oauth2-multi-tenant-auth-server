package ktor.oauth2.multi.tenant.auth.server.persistence.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalTime::class)
@Serializable
data class UserTrustedDeviceData(
    val id: Long? = null,
    val user: UserData,
    val instanceId: String? = null,
    val deviceIp: String? = null,
    val userAgent: String? = null,
    val createdDate: String? = null,
    val updatedDate: String? = null,
)

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class UserTrustedDevice(id: EntityID<Long>) : LongEntity(id), DtoConverter<UserTrustedDeviceData> {
    companion object : LongEntityClass<UserTrustedDevice>(UserTrustedDevices)

    var user by User referencedOn UserTrustedDevices.user
    var instanceId by UserTrustedDevices.instanceId
    var deviceIp by UserTrustedDevices.deviceIp
    var userAgent by UserTrustedDevices.userAgent
    var createdDate by UserTrustedDevices.createdDate
    var updatedDate by UserTrustedDevices.updatedDate

    override fun toDto(): UserTrustedDeviceData {
        return UserTrustedDeviceData(
            id.value,
            user.toData(),
            instanceId,
            deviceIp,
            userAgent,
            createdDate.toString(),
            updatedDate.toString(),
        )
    }
}

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
object UserTrustedDevices : LongIdTable("user_trusted_device") {
    val user = reference("user_id", Users)
    val instanceId: Column<String?> = varchar("instance_id", length = 255).nullable()
    val deviceIp: Column<String?> = varchar("device_ip", length = 255).nullable()
    val userAgent: Column<String?> = varchar("user_agent", length = 255).nullable()
    val createdDate = timestamp("created_date")
    val updatedDate = timestamp("updated_date").nullable()
}
