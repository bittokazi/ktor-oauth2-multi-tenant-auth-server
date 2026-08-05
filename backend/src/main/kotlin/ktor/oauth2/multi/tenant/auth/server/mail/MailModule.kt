package ktor.oauth2.multi.tenant.auth.server.mail

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import ktor.oauth2.multi.tenant.auth.server.mail.config.MailConfig
import ktor.oauth2.multi.tenant.auth.server.mail.services.DefaultMailService
import ktor.oauth2.multi.tenant.auth.server.mail.services.MailGeneratorService
import ktor.oauth2.multi.tenant.auth.server.mail.services.MailService
import ktor.oauth2.multi.tenant.auth.server.mail.services.NoopMailService

fun Application.configureMailModule() {
    dependencies {
        provide(MailConfig::class)
        provide(MailGeneratorService::class)
    }

    val mailConfig: MailConfig by dependencies

    dependencies {
        provide<MailService>(
            when (mailConfig.enabled) {
                true -> DefaultMailService::class

                false -> NoopMailService::class
            },
        )
    }
}
