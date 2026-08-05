package ktor.oauth2.multi.tenant.auth.server.security.config

import com.bittokazi.ktor.auth.services.TemplateCustomizer
import com.bittokazi.ktor.auth.services.TemplateCustomizerFactory
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.MustacheFactory
import io.ktor.server.application.*
import io.ktor.util.AttributeKey
import ktor.oauth2.multi.tenant.auth.server.config.AppConfig
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.TenantData
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.TenantRepository
import java.io.File

class OauthTemplateCustomizerImpl(
    val tenantRepository: TenantRepository,
    val oauthConfig: OauthConfig,
) : TemplateCustomizer, TemplateCustomizerFactory {
    override fun addExtraData(call: ApplicationCall): Map<String, Any> {
        val tenantKey = call.attributes[AttributeKey<String>(AppConfig.TENANT_ATTRIBUTE_KEY)]
        val default =
            mapOf<String, Any>(
                "appName" to oauthConfig.appName,
            )
        return when (tenantKey) {
            "public" -> default

            else -> {
                when (val tenant = tenantRepository.findOneByCompanyKey(tenantKey)) {
                    is TenantData ->
                        mapOf(
                            "appName" to tenant.name,
                            "resourceBase" to "/app-cpanel-static/${tenant.companyKey}",
                        )

                    else -> default
                }
            }
        }
    }

    override fun getFactory(call: ApplicationCall): MustacheFactory {
        return when (val tenantKey = call.attributes[AttributeKey<String>(AppConfig.TENANT_ATTRIBUTE_KEY)]) {
            "public" -> DefaultMustacheFactory("templates")

            else -> {
                when (val tenant = tenantRepository.findOneByCompanyKey(tenantKey)) {
                    is TenantData -> {
                        when (tenant.enableCustomTemplate) {
                            true ->
                                DefaultMustacheFactory(
                                    File(
                                        oauthConfig.templateFolder + File.separatorChar + tenant.companyKey,
                                    ),
                                )

                            false -> DefaultMustacheFactory("templates")
                        }
                    }

                    else -> DefaultMustacheFactory("templates")
                }
            }
        }
    }
}
