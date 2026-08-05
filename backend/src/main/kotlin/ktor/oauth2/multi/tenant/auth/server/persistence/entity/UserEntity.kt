package ktor.oauth2.multi.tenant.auth.server.persistence.entity

import kotlinx.serialization.Serializable
import ktor.oauth2.multi.tenant.auth.server.utils.Utils
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class)
@Serializable
data class UserData(
    val id: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    var password: String = "",
    val newPassword: String? = null,
    var roles: List<RoleDto>?,
    val image: String? = "https://www.gravatar.com/avatar/${Utils.md5(email)}?d=identicon",
    var adminTenantUser: Boolean = false,
    var twoFaEnabled: Boolean = false,
)

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class User(id: EntityID<String>) : Entity<String>(id), DtoConverter<UserData> {
    companion object : EntityClass<String, User>(Users)

    var firstName by Users.firstName
    var lastName by Users.lastName
    var email by Users.email
    var password: String? = ""
    var hashedPassword by Users.hashedPassword
    var roles by Role.via(UserRoles)
    var twoFaEnabled by Users.twoFaEnabled

    fun toData(addPassword: Boolean = false): UserData {
        return UserData(
            id.value,
            firstName,
            lastName,
            email,
            if (addPassword) hashedPassword else "",
            "",
            roles.let { iterable ->
                iterable.toList().map {
                    it.toDto()
                }
            },
            twoFaEnabled = twoFaEnabled,
        )
    }

    override fun toDto(): UserData {
        return toData()
    }
}

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
object Users : IdTable<String>("user") {
    override val id: Column<EntityID<String>> =
        varchar("id", 255)
            .clientDefault { Uuid.random().toString() }
            .entityId()
    val firstName: Column<String> = varchar("first_name", length = 255)
    val lastName: Column<String> = varchar("last_name", length = 255)
    val email: Column<String> = varchar("email", length = 255)
    val hashedPassword: Column<String> = varchar("password", length = 255)
    val twoFaEnabled: Column<Boolean> = bool("two_fa_enabled").default(false)
}

@OptIn(ExperimentalUuidApi::class)
object UserRoles : Table("user_role") {
    val userId = reference("user_id", Users.id)
    val roleId = reference("role_id", Roles.id)
    override val primaryKey = PrimaryKey(userId, roleId)
}
