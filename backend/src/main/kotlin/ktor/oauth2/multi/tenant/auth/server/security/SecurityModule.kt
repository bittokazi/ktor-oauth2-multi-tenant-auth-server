package ktor.oauth2.multi.tenant.auth.server.security

import com.bittokazi.ktor.auth.configureOauth2AuthorizationServer
import com.bittokazi.ktor.auth.database.OauthDatabaseConfiguration
import com.bittokazi.ktor.auth.oauthAuthenticationConfig
import com.bittokazi.ktor.auth.services.JwksProvider
import com.bittokazi.ktor.auth.services.JwtTokenCustomizer
import com.bittokazi.ktor.auth.services.JwtVerifier
import com.bittokazi.ktor.auth.services.SessionCustomizer
import com.bittokazi.ktor.auth.services.SessionExtender
import com.bittokazi.ktor.auth.services.TemplateCustomizer
import com.bittokazi.ktor.auth.services.TemplateCustomizerFactory
import com.bittokazi.ktor.auth.services.issuer.IssuerProvider
import com.bittokazi.ktor.auth.services.providers.OauthAuthorizationCodeService
import com.bittokazi.ktor.auth.services.providers.OauthClientService
import com.bittokazi.ktor.auth.services.providers.OauthConsentService
import com.bittokazi.ktor.auth.services.providers.OauthDeviceCodeService
import com.bittokazi.ktor.auth.services.providers.OauthLoginOptionService
import com.bittokazi.ktor.auth.services.providers.OauthLogoutActionService
import com.bittokazi.ktor.auth.services.providers.OauthTokenService
import com.bittokazi.ktor.auth.services.providers.OauthUserService
import com.bittokazi.ktor.auth.services.providers.database.OauthAuthorizationCodeServiceDatabaseProvider
import com.bittokazi.ktor.auth.services.providers.database.OauthClientServiceDatabaseProvider
import com.bittokazi.ktor.auth.services.providers.database.OauthConsentServiceDatabaseProvider
import com.bittokazi.ktor.auth.services.providers.database.OauthDeviceCodeServiceDatabaseProvider
import com.bittokazi.ktor.auth.services.providers.database.OauthTokenServiceDatabaseProvider
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authentication
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.requestvalidation.RequestValidation
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.TenantRepository
import ktor.oauth2.multi.tenant.auth.server.security.config.ExtraTokenClaimConfig
import ktor.oauth2.multi.tenant.auth.server.security.config.IssuerProviderImpl
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

            if (oauthConfig.enableIssuerProvider) {
                provide<IssuerProvider>(IssuerProviderImpl::class)
            }
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
