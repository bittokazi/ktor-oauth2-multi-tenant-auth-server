package ktor.oauth2.multi.tenant.auth.server.persistence.repository

import io.ktor.server.application.ApplicationCall
import ktor.oauth2.multi.tenant.auth.server.database.config.MultiTenantDatabaseConfiguration
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PaginatedResponse
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PaginationRequest
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.Tenant
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.TenantData
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.Tenants
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TenantRepository(
    multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : BaseRepository(multiTenantDatabaseConfiguration) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[TenantRepository] -> created")
    }

    fun findOneByCompanyKey(companyKey: String): TenantData? {
        return query(schema = "public") {
            Tenant.find {
                Tenants.companyKey eq companyKey
            }.singleOrNull()?.toData() ?: run { null }
        }
    }

    fun findOneById(
        id: Long,
        call: ApplicationCall,
    ): TenantData? {
        return query(call = call) {
            Tenant.find {
                Tenants.id eq id
            }.singleOrNull()?.toData() ?: run { null }
        }
    }

    fun findOneByName(
        name: String,
        call: ApplicationCall,
    ): TenantData? {
        return query(call = call) {
            Tenant.find {
                Tenants.name eq name
            }.singleOrNull()?.toData() ?: run { null }
        }
    }

    fun findOneByDomain(domain: String): TenantData? {
        return query(schema = "public") {
            Tenant.find {
                Tenants.domain eq domain
            }.singleOrNull()?.toData() ?: run { null }
        }
    }

    fun getAll(call: ApplicationCall): List<TenantData> {
        return query(call = call) {
            Tenant.all().map { it.toData() }
        }
    }

    fun getAllPaginated(
        paginationRequest: PaginationRequest,
        call: ApplicationCall,
    ): PaginatedResponse<TenantData> {
        return query(call = call) {
            var q = Tenants.selectAll()

            paginationRequest.query?.split("&")?.forEach { pair ->
                val parts = pair.split("=", limit = 2)
                if (parts.size == 2 && parts[0] == "name") {
                    q = q.andWhere { Tenants.name eq parts[1] }
                }
            }

            val limit = paginationRequest.count?.toLong() ?: 10L
            val page = paginationRequest.page?.toLong() ?: 1L

            getPaginatedResponse(
                Tenant.wrapRows(q),
                limit,
                page,
            ) {
                it.orderBy(Tenants.id to SortOrder.DESC)
            }
        }
    }

    fun insert(
        tenantData: TenantData,
        call: ApplicationCall,
    ): TenantData {
        return query(call = call) {
            Tenant.new {
                companyKey = tenantData.companyKey
                enabled = tenantData.enabled
                name = tenantData.name
                domain = tenantData.domain
                logo = tenantData.logo
                logoAbsolutePath = tenantData.logoAbsolutePath
                signInBtnColor = tenantData.signInBtnColor
                resetPasswordLink = tenantData.resetPasswordLink
                createAccountLink = tenantData.createAccountLink
                defaultRedirectUrl = tenantData.defaultRedirectUrl
                enableConfigPanel = tenantData.enableConfigPanel
                enableCustomTemplate = tenantData.enableCustomTemplate
                customTemplateLocation = tenantData.customTemplateLocation
            }.toData()
        }
    }

    fun update(
        id: Long,
        tenantData: TenantData,
        call: ApplicationCall,
    ): TenantData? {
        return query(call = call) {
            Tenant.findByIdAndUpdate(id) { tenant ->
                tenant.companyKey = tenantData.companyKey
                tenant.enabled = tenantData.enabled
                tenant.name = tenantData.name
                tenant.domain = tenantData.domain
                tenant.logo = tenantData.logo
                tenant.logoAbsolutePath = tenantData.logoAbsolutePath
                tenant.signInBtnColor = tenantData.signInBtnColor
                tenant.resetPasswordLink = tenantData.resetPasswordLink
                tenant.createAccountLink = tenantData.createAccountLink
                tenant.defaultRedirectUrl = tenantData.defaultRedirectUrl
                tenant.enableConfigPanel = tenantData.enableConfigPanel
                tenant.enableCustomTemplate = tenantData.enableCustomTemplate
                tenant.customTemplateLocation = tenantData.customTemplateLocation
            }?.toData()
        }
    }

    fun delete(
        id: Long,
        call: ApplicationCall,
    ): Boolean {
        return query(call = call) {
            Tenant.findById(id)?.let {
                it.delete()
                true
            } ?: false
        }
    }
}
