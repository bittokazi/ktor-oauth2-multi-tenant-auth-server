package ktor.oauth2.multi.tenant.auth.server.app

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import ktor.oauth2.multi.tenant.auth.server.app.client.oauthClientModule
import ktor.oauth2.multi.tenant.auth.server.app.config.AppModule
import ktor.oauth2.multi.tenant.auth.server.app.config.AppModuleConfiguration
import ktor.oauth2.multi.tenant.auth.server.app.config.infoModule
import ktor.oauth2.multi.tenant.auth.server.app.cpanel.cpanelModule
import ktor.oauth2.multi.tenant.auth.server.app.password.reset.passwordResetModule
import ktor.oauth2.multi.tenant.auth.server.app.role.roleModule
import ktor.oauth2.multi.tenant.auth.server.app.tenant.tenantModule
import ktor.oauth2.multi.tenant.auth.server.app.user.userModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Application.configureAppModule() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    dependencies {
        provide(AppModuleConfiguration::class)
    }

    val appModuleConfiguration: AppModuleConfiguration by dependencies

    val appInfo = infoModule()

    if (appModuleConfiguration.modules[AppModule.USER]?.enabled == true) {
        userModule()
    }
    if (appModuleConfiguration.modules[AppModule.CPANEL]?.enabled == true) {
        cpanelModule()
    }
    if (appModuleConfiguration.modules[AppModule.CLIENT]?.enabled == true) {
        oauthClientModule()
    }
    if (appModuleConfiguration.modules[AppModule.PASSWORD_RESET]?.enabled == true) {
        passwordResetModule()
    }
    if (appModuleConfiguration.modules[AppModule.ROLE]?.enabled == true) {
        roleModule()
    }
    if (appModuleConfiguration.modules[AppModule.TENANT]?.enabled == true) {
        tenantModule()
    }

    log.info("[AppModule] -> init with version: ${appInfo.version}")
}
