package ktor.oauth2.multi.tenant.auth.server.persistence.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@Serializable
@OptIn(ExperimentalTime::class)
data class PasswordResetTokenData(
    val id: Long? = null,
    val user: UserData,
    var token: String,
    var expireDate: Instant,
)

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class PasswordResetToken(id: EntityID<Long>) :
    LongEntity(id),
    DtoConverter<PasswordResetTokenData> {
    companion object : LongEntityClass<PasswordResetToken>(PasswordResetTokens)

    var user by User referencedOn PasswordResetTokens.user
    var token by PasswordResetTokens.token
    var expireDate by PasswordResetTokens.expireDate

    override fun toDto(): PasswordResetTokenData {
        return PasswordResetTokenData(
            id.value,
            user.toData(false),
            token,
            expireDate,
        )
    }
}

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
object PasswordResetTokens : LongIdTable("password_reset_token") {
    val user = reference("user_id", Users)
    val token: Column<String> = varchar("token", length = 255)
    val expireDate: Column<Instant> = timestamp("expire_date")
}
