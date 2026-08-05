package ktor.oauth2.multi.tenant.auth.server.persistence.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class UserTwoFaSecretData(
    val id: Long? = null,
    val user: UserData,
    val secret: String? = null,
    var scratchCodes: String? = null,
    val tenantName: String? = null,
)

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class UserTwoFaSecret(id: EntityID<Long>) : LongEntity(id), DtoConverter<UserTwoFaSecretData> {
    companion object : LongEntityClass<UserTwoFaSecret>(UserTwoFaSecrets)

    var user by User referencedOn UserTwoFaSecrets.user
    var secret by UserTwoFaSecrets.secret
    var scratchCodes by UserTwoFaSecrets.scratchCodes

    // tenantName was transient in the original JPA model; keep it in DTO only

    override fun toDto(): UserTwoFaSecretData {
        return UserTwoFaSecretData(
            id.value,
            user.toData(),
            secret,
            scratchCodes,
            null,
        )
    }
}

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
object UserTwoFaSecrets : LongIdTable("user_two_fa_secret") {
    val user = reference("user_id", Users)
    val secret: Column<String?> = varchar("secret", length = 255).nullable()
    val scratchCodes: Column<String?> = varchar("scratch_codes", length = 2000).nullable()
}
