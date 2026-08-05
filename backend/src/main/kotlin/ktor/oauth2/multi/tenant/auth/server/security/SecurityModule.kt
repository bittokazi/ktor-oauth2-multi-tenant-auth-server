package ktor.oauth2.multi.tenant.auth.server.security

import com.bittokazi.ktor.auth.configureOauth2AuthorizationServer
import com.bittokazi.ktor.auth.database.OauthDatabaseConfiguration
import com.bittokazi.ktor.auth.oauthAuthenticationConfig
import com.bittokazi.ktor.auth.services.*
import com.bittokazi.ktor.auth.services.providers.*
import com.bittokazi.ktor.auth.services.providers.database.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.plugins.requestvalidation.*
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.TenantRepository
import ktor.oauth2.multi.tenant.auth.server.security.config.ExtraTokenClaimConfig
import ktor.oauth2.multi.tenant.auth.server.security.config.JwtCustomizerImpl
import ktor.oauth2.multi.tenant.auth.server.security.config.LoginOptionServiceImpl
import ktor.oauth2.multi.tenant.auth.server.security.config.LogoutActionCustomizerImpl
import ktor.oauth2.multi.tenant.auth.server.security.config.OauthConfig
import ktor.oauth2.multi.tenant.auth.server.security.config.OauthTemplateCustomizerImpl
import ktor.oauth2.multi.tenant.auth.server.security.config.SessionExtenderImpl
import ktor.oauth2.multi.tenant.auth.server.security.config.tenantInterceptorPlugin
import ktor.oauth2.multi.tenant.auth.server.security.controllers.otpRoute
import ktor.oauth2.multi.tenant.auth.server.security.database.OauthDatabaseConfigurationImpl
import ktor.oauth2.multi.tenant.auth.server.security.services.DefaultTwoFaService
import ktor.oauth2.multi.tenant.auth.server.security.services.LoginService
import ktor.oauth2.multi.tenant.auth.server.security.services.OauthUserServiceImpl
import ktor.oauth2.multi.tenant.auth.server.security.services.TwoFaService

fun Application.configureSecurityModule() {
    dependencies {
        provide<OauthConfig>(OauthConfig::class)
        provide<ExtraTokenClaimConfig>(ExtraTokenClaimConfig::class)
    }

    val oauthConfig: OauthConfig by dependencies
    val tenantRepository: TenantRepository by dependencies

    if (oauthConfig.enabled) {
        dependencies {
            provide<OauthDatabaseConfiguration>(OauthDatabaseConfigurationImpl::class)
            provide<OauthClientService>(OauthClientServiceDatabaseProvider::class)
            provide<OauthUserService>(OauthUserServiceImpl::class)
        }

        authentication {
            oauthAuthenticationConfig(oauthConfig.issuer)
        }
        dependencies {
            provide<OauthAuthorizationCodeService>(OauthAuthorizationCodeServiceDatabaseProvider::class)
            provide<OauthTokenService>(OauthTokenServiceDatabaseProvider::class)
            provide<OauthConsentService>(OauthConsentServiceDatabaseProvider::class)
            provide<OauthDeviceCodeService>(OauthDeviceCodeServiceDatabaseProvider::class)

            provide<JwtTokenCustomizer>(JwtCustomizerImpl::class)
            provide(JwksProvider::class)
            provide(JwtVerifier::class)
            provide(SessionCustomizer::class)
            provide<TemplateCustomizer>(OauthTemplateCustomizerImpl::class)
            provide<OauthLoginOptionService>(LoginOptionServiceImpl::class)
            provide<OauthLogoutActionService>(LogoutActionCustomizerImpl::class)

            provide<SessionExtender>(SessionExtenderImpl::class)

            provide(LoginService::class)
            provide<TwoFaService>(DefaultTwoFaService::class)
            provide<TemplateCustomizerFactory>(OauthTemplateCustomizerImpl::class)
        }

        configureOauth2AuthorizationServer(
            configureSerialization = false,
            defaultLoginRoutes = true,
            defaultAuthorizeRoute = true,
            defaultOidcRoute = true,
            defaultTokenRoute = true,
            defaultConsentRoute = true,
            configureForwardHeaderAndDefaultHeadersPlugin = false,
        )
    }

    otpRoute()

    install(tenantInterceptorPlugin(tenantRepository))

    install(RequestValidation)
}
