package ktor.oauth2.multi.tenant.auth.server.app.tenant.services

import io.ktor.server.application.ApplicationCall
import kotlinx.serialization.Serializable
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.TenantData
import ktor.oauth2.multi.tenant.auth.server.storage.service.FileErrorCode

interface TenantService {
    fun getTenantInfo(call: ApplicationCall): CallResult<TenantInfo, TenantServiceError>

    fun getAll(call: ApplicationCall): List<TenantData>

    fun get(call: ApplicationCall): CallResult<TenantData, TenantServiceError>

    fun add(
        tenantData: TenantData,
        call: ApplicationCall,
    ): CallResult<TenantData, TenantServiceError>

    fun update(
        call: ApplicationCall,
        tenantData: TenantData,
    ): CallResult<TenantData, TenantServiceError>

    suspend fun uploadTemplate(
        call: ApplicationCall,
        fn: suspend (CallResult<Map<String, String>, FileErrorCode>) -> Unit,
    )
}

@Serializable
data class TenantInfo(
    val cpanel: Boolean,
    val enabledConfigPanel: Boolean,
    val name: String,
    val systemVersion: String = "v0.0.0_Dev",
)

enum class TenantServiceError {
    NOT_FOUND,
    UNPROCESSABLE_ENTITY,
}
