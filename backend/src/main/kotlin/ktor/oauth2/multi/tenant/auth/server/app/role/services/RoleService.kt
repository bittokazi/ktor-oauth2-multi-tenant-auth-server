package ktor.oauth2.multi.tenant.auth.server.app.role.services

import io.ktor.server.application.ApplicationCall
import kotlinx.serialization.Serializable
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.RoleDto
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.RoleRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface RoleService {
    fun findAll(call: ApplicationCall): List<RoleDto>

    fun get(
        id: String,
        call: ApplicationCall,
    ): CallResult<RoleDto, RoleServiceError>

    fun create(
        roleDto: RoleDto,
        call: ApplicationCall,
    ): CallResult<RoleDto, RoleServiceError>

    fun update(
        id: String,
        roleDto: RoleDto,
        call: ApplicationCall,
    ): CallResult<RoleDto, RoleServiceError>
}

class DefaultRoleService(
    private val roleRepository: RoleRepository,
) : RoleService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[RoleService] -> init")
    }

    override fun findAll(call: ApplicationCall): List<RoleDto> {
        return roleRepository.findAll(call = call)
    }

    override fun get(
        id: String,
        call: ApplicationCall,
    ): CallResult<RoleDto, RoleServiceError> {
        return roleRepository.findOneById(id, call = call)?.let {
            CallResult.Success(it)
        } ?: CallResult.Failure(RoleServiceError.NOT_FOUND)
    }

    override fun create(
        roleDto: RoleDto,
        call: ApplicationCall,
    ): CallResult<RoleDto, RoleServiceError> {
        return roleRepository.findOneByRoleKey(roleDto.roleKey, call = call)?.let {
            CallResult.Failure(
                RoleServiceError.ROLE_KEY_EXIST,
                errorData = mapOf("roleKey" to listOf("exist")),
            )
        } ?: run {
            try {
                val createdRole = roleRepository.insert(roleDto, call = call)
                CallResult.Success(createdRole)
            } catch (e: Exception) {
                log.error("[RoleService] Error creating role: ${e.message}", e)
                CallResult.Failure(RoleServiceError.UNPROCESSABLE_ENTITY)
            }
        }
    }

    override fun update(
        id: String,
        roleDto: RoleDto,
        call: ApplicationCall,
    ): CallResult<RoleDto, RoleServiceError> {
        return roleRepository.findOneById(id, call = call)?.let {
            try {
                val updatedRole = roleRepository.update(id, roleDto, call = call)
                updatedRole?.let {
                    CallResult.Success(it)
                } ?: CallResult.Failure(RoleServiceError.NOT_FOUND)
            } catch (e: Exception) {
                log.error("[RoleService] Error updating role: ${e.message}", e)
                CallResult.Failure(RoleServiceError.UNPROCESSABLE_ENTITY)
            }
        } ?: CallResult.Failure(RoleServiceError.NOT_FOUND)
    }
}

enum class RoleServiceError {
    NOT_FOUND,
    ROLE_KEY_EXIST,
    UNPROCESSABLE_ENTITY,
}

@Serializable
data class RoleErrorResponse(
    val errors: Map<String, List<String>> = emptyMap(),
)
