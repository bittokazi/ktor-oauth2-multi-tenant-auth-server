package ktor.oauth2.multi.tenant.auth.server.persistence.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

@Serializable
data class TenantData(
    val id: Long? = null,
    val companyKey: String = "",
    val enabled: Boolean = false,
    val name: String = "",
    val domain: String? = null,
    val logo: String? = null,
    val logoAbsolutePath: String? = null,
    val signInBtnColor: String? = null,
    val resetPasswordLink: String? = null,
    val createAccountLink: String? = null,
    val defaultRedirectUrl: String? = null,
    val enableConfigPanel: Boolean = false,
    val enableCustomTemplate: Boolean = false,
    val customTemplateLocation: String? = null,
)

class Tenant(id: EntityID<Long>) : LongEntity(id), DtoConverter<TenantData> {
    companion object : LongEntityClass<Tenant>(Tenants)

    var companyKey by Tenants.companyKey
    var enabled by Tenants.enabled
    var name by Tenants.name
    var domain by Tenants.domain
    var logo by Tenants.logo
    var logoAbsolutePath by Tenants.logoAbsolutePath
    var signInBtnColor by Tenants.signInBtnColor
    var resetPasswordLink by Tenants.resetPasswordLink
    var createAccountLink by Tenants.createAccountLink
    var defaultRedirectUrl by Tenants.defaultRedirectUrl
    var enableConfigPanel by Tenants.enableConfigPanel
    var enableCustomTemplate by Tenants.enableCustomTemplate
    var customTemplateLocation by Tenants.customTemplateLocation

    fun toData(): TenantData {
        return TenantData(
            id.value,
            companyKey,
            enabled,
            name,
            domain,
            logo,
            logoAbsolutePath,
            signInBtnColor,
            resetPasswordLink,
            createAccountLink,
            defaultRedirectUrl,
            enableConfigPanel,
            enableCustomTemplate,
            customTemplateLocation,
        )
    }

    override fun toDto(): TenantData {
        return toData()
    }
}

object Tenants : LongIdTable("tenant") {
    val companyKey: Column<String> = varchar("company_key", length = 255).uniqueIndex()
    val enabled: Column<Boolean> = bool("enabled")
    val name: Column<String> = varchar("name", length = 255).uniqueIndex()
    val domain: Column<String?> = varchar("domain", length = 255).nullable()
    val logo: Column<String?> = varchar("logo", length = 255).nullable()
    val logoAbsolutePath: Column<String?> = varchar("logo_absolute_path", length = 255).nullable()
    val signInBtnColor: Column<String?> = varchar("signin_btn_color", length = 255).nullable()
    val resetPasswordLink: Column<String?> = varchar("reset_password_link", length = 255).nullable()
    val createAccountLink: Column<String?> = varchar("create_account_link", length = 255).nullable()
    val defaultRedirectUrl: Column<String?> = varchar("default_redirect_url", length = 255).nullable()
    val enableConfigPanel: Column<Boolean> = bool("enable_config_panel")
    val enableCustomTemplate: Column<Boolean> = bool("enable_custom_template")
    val customTemplateLocation: Column<String?> = varchar("custom_template_location", length = 255).nullable()
}
