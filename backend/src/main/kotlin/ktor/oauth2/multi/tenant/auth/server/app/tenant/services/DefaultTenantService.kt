package ktor.oauth2.multi.tenant.auth.server.app.tenant.services

import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey
import ktor.oauth2.multi.tenant.auth.server.config.AppConfig
import ktor.oauth2.multi.tenant.auth.server.database.config.MultiTenantDatabaseConfiguration
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.TenantData
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.TenantRepository
import ktor.oauth2.multi.tenant.auth.server.storage.service.FileErrorCode
import ktor.oauth2.multi.tenant.auth.server.storage.service.FileService
import org.slf4j.LoggerFactory

class DefaultTenantService(
    private val tenantRepository: TenantRepository,
    private val multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
    private val fileService: FileService,
) : TenantService {
    private val log = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[TenantService] -> init")
    }

    override fun getTenantInfo(call: ApplicationCall): CallResult<TenantInfo, TenantServiceError> {
        return call.attributes[AttributeKey<String>(AppConfig.TENANT_ATTRIBUTE_KEY)].let {
            when (it) {
                "public" ->
                    CallResult.Success(
                        TenantInfo(
                            true,
                            enabledConfigPanel = true,
                            name = "Auth Kit",
                        ),
                    )
                else ->
                    tenantRepository.findOneByCompanyKey(it)?.let { tenant ->
                        CallResult.Success(
                            TenantInfo(
                                false,
                                enabledConfigPanel = tenant.enableConfigPanel,
                                name = tenant.name,
                            ),
                        )
                    } ?: run {
                        CallResult.Failure(TenantServiceError.NOT_FOUND)
                    }
            }
        }
    }

    override fun getAll(call: ApplicationCall): List<TenantData> {
        return tenantRepository.getAll(call = call)
    }

    override fun get(call: ApplicationCall): CallResult<TenantData, TenantServiceError> {
        return call.parameters["id"]?.let { idStr ->
            val id = idStr.toLongOrNull()
            id?.let {
                when (val existing = tenantRepository.findOneById(it, call = call)) {
                    is TenantData -> CallResult.Success(existing)
                    else -> CallResult.Failure(TenantServiceError.NOT_FOUND)
                }
            } ?: CallResult.Failure(TenantServiceError.NOT_FOUND)
        } ?: CallResult.Failure(TenantServiceError.NOT_FOUND)
    }

    override fun add(
        tenantData: TenantData,
        call: ApplicationCall,
    ): CallResult<TenantData, TenantServiceError> {
        return try {
            val createdTenant = tenantRepository.insert(tenantData, call = call)
            multiTenantDatabaseConfiguration.createDatabaseSchema(schema = createdTenant.companyKey)
            CallResult.Success(createdTenant)
        } catch (e: Exception) {
            log.error("[TenantService] Error creating tenant: ${e.message}", e)
            CallResult.Failure(TenantServiceError.UNPROCESSABLE_ENTITY)
        }
    }

    override fun update(
        call: ApplicationCall,
        tenantData: TenantData,
    ): CallResult<TenantData, TenantServiceError> {
        return when (tenantData.id) {
            is Long -> {
                when (val existing = tenantRepository.findOneById(tenantData.id, call = call)) {
                    is TenantData -> CallResult.Success(tenantRepository.update(tenantData.id, tenantData, call = call)!!)
                    else -> CallResult.Failure(TenantServiceError.NOT_FOUND)
                }
            }

            else -> CallResult.Failure(TenantServiceError.UNPROCESSABLE_ENTITY)
        }
    }

    override suspend fun uploadTemplate(
        call: ApplicationCall,
        fn: suspend (CallResult<Map<String, String>, FileErrorCode>) -> Unit,
    ) {
        call.parameters["id"]?.let { idStr ->
            val id = idStr.toLongOrNull()
            id?.let {
                when (val existing = tenantRepository.findOneById(it, call = call)) {
                    is TenantData ->
                        fileService.upload(existing.companyKey, call) { result ->
                            when (result) {
                                is CallResult.Failure -> fn(CallResult.Failure(result.errorCode))
                                is CallResult.Success -> {
                                    fn(
                                        CallResult.Success(
                                            mapOf(
                                                "location" to result.outcome,
                                            ),
                                        ),
                                    )
                                }
                            }
                        }
                    else -> fn(CallResult.Failure(FileErrorCode.TENANT_NOT_FOUND))
                }
            } ?: fn(CallResult.Failure(FileErrorCode.TENANT_NOT_FOUND))
        } ?: fn(CallResult.Failure(FileErrorCode.UNPROCESSABLE_ENTITY))
    }
}
