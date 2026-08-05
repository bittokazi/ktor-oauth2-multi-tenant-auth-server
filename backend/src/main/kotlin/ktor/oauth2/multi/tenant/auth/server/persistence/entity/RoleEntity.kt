package ktor.oauth2.multi.tenant.auth.server.persistence.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class RoleDto(
    val id: String? = null,
    val name: String = "",
    val roleKey: String = "",
)

@OptIn(ExperimentalUuidApi::class)
class Role(id: EntityID<String>) : Entity<String>(id), DtoConverter<RoleDto> {
    companion object : EntityClass<String, Role>(Roles)

    var name by Roles.name
    var roleKey by Roles.roleKey

    override fun toDto(): RoleDto {
        return RoleDto(id.value, name, roleKey)
    }
}

@OptIn(ExperimentalUuidApi::class)
object Roles : IdTable<String>("role") {
    override val id: Column<EntityID<String>> =
        varchar("id", 255)
            .clientDefault { Uuid.random().toString() }
            .entityId()
    val name: Column<String> = varchar("name", length = 255)
    val roleKey: Column<String> = varchar("role_key", length = 255)
}
