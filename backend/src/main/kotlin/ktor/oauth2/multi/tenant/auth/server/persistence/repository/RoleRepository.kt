package ktor.oauth2.multi.tenant.auth.server.persistence.repository

import io.ktor.server.application.ApplicationCall
import ktor.oauth2.multi.tenant.auth.server.database.config.MultiTenantDatabaseConfiguration
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.Role
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.RoleDto
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.RoleEnum
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.Roles
import org.jetbrains.exposed.v1.core.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RoleRepository(
    multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : BaseRepository(multiTenantDatabaseConfiguration) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[RoleRepository] -> created")
    }

    fun findOneByKey(
        roleKey: RoleEnum,
        call: ApplicationCall,
    ): RoleDto? {
        return query(call = call) {
            Role.find {
                Roles.roleKey eq roleKey.toString()
            }.singleOrNull()?.toDto() ?: run { null }
        }
    }

    fun findOneByRoleKey(
        roleKey: String,
        call: ApplicationCall,
    ): RoleDto? {
        return query(call = call) {
            Role.find {
                Roles.roleKey eq roleKey
            }.singleOrNull()?.toDto() ?: run { null }
        }
    }

    fun findOneById(
        id: String,
        call: ApplicationCall,
    ): RoleDto? {
        return query(call = call) {
            Role.findById(id)?.toDto() ?: run { null }
        }
    }

    fun findAll(call: ApplicationCall): List<RoleDto> {
        return query(call = call) {
            Role.all().map { it.toDto() }
        }
    }

    fun insert(
        roleDto: RoleDto,
        call: ApplicationCall,
    ): RoleDto {
        return query(call = call) {
            Role.new(roleDto.id) {
                name = roleDto.name
                roleKey = roleDto.roleKey
            }.toDto()
        }
    }

    fun update(
        id: String,
        roleDto: RoleDto,
        call: ApplicationCall,
    ): RoleDto? {
        return query(call = call) {
            Role.findByIdAndUpdate(id) { role ->
                role.name = roleDto.name
            }?.toDto()
        }
    }
}
