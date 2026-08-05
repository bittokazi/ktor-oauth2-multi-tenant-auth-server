package ktor.oauth2.multi.tenant.auth.server.security.config

import com.bittokazi.ktor.auth.utils.getBaseUrl
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.util.AttributeKey
import ktor.oauth2.multi.tenant.auth.server.config.AppConfig.Companion.AUTH_TENANT_ATTRIBUTE_KEY
import ktor.oauth2.multi.tenant.auth.server.config.AppConfig.Companion.TENANT_ATTRIBUTE_KEY
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.TenantRepository

fun tenantInterceptorPlugin(tenantRepository: TenantRepository): ApplicationPlugin<Unit> {
    return createApplicationPlugin(name = "tenantInterceptorPlugin") {
        onCall { call ->

            call.request.header("X-DATA-TENANT")?.let {
                tenantRepository.findOneByCompanyKey(it)?.let { tenant ->
                    if (!tenant.enabled) {
                        return@onCall call.respond(
                            status = HttpStatusCode.Forbidden,
                            message = "Access denied for tenant ${tenant.companyKey} as it is disabled.",
                        )
                    }
                    call.attributes.put(AttributeKey<String>(TENANT_ATTRIBUTE_KEY), tenant.companyKey)
                } ?: run {
                    call.attributes.put(AttributeKey<String>(TENANT_ATTRIBUTE_KEY), "public")
                }
            } ?: run {
                tenantRepository
                    .findOneByDomain(
                        call.getBaseUrl()
                            .replace("http://", "")
                            .replace("https://", "")
                            .replace("www.", ""),
                    )?.let {
                        if (!it.enabled) {
                            return@onCall call.respond(
                                status = HttpStatusCode.Forbidden,
                                message = "Access denied for tenant ${it.companyKey} as it is disabled.",
                            )
                        }
                        call.attributes.put(AttributeKey<String>(TENANT_ATTRIBUTE_KEY), it.companyKey)
                    } ?: run {
                    call.attributes.put(AttributeKey<String>(TENANT_ATTRIBUTE_KEY), "public")
                }
            }

            tenantRepository
                .findOneByDomain(
                    call.getBaseUrl()
                        .replace("http://", "")
                        .replace("https://", "")
                        .replace("www.", ""),
                )?.let {
                    call.attributes.put(AttributeKey<String>(AUTH_TENANT_ATTRIBUTE_KEY), it.companyKey)
                } ?: run {
                call.attributes.put(AttributeKey<String>(AUTH_TENANT_ATTRIBUTE_KEY), "public")
            }
        }
    }
}
