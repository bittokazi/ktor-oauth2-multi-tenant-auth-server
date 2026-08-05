package ktor.oauth2.multi.tenant.auth.server.mail.config

import io.ktor.server.plugins.di.annotations.Property

data class MailConfig(
    @Property("mail-config.enabled") val enabled: Boolean = false,
    @Property("mail-config.host") val host: String = "",
    @Property("mail-config.port") val port: Int = 0,
    @Property("mail-config.auth") val auth: Boolean = false,
    @Property("mail-config.starttls") val tls: Boolean = false,
    @Property("mail-config.username") val username: String = "",
    @Property("mail-config.password") val password: String = "",
    @Property("mail-config.from") val from: String = "",
)
