package ktor.oauth2.multi.tenant.auth.server.mail.services

import ktor.oauth2.multi.tenant.auth.server.app.config.AppModuleConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MailGeneratorService(
    val appModuleConfiguration: AppModuleConfiguration,
) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[MailGeneratorService] -> init")
    }

    fun generateResetPasswordMail(data: Map<ResetPasswordMailLabel, String>): String {
        val username = data[ResetPasswordMailLabel.USERNAME] ?: "User"
        val resetLink = data[ResetPasswordMailLabel.RESET_LINK] ?: "#"

        return """
            <html>
                <body>
                    <p>Hi $username,</p>
                    <p>You requested a password reset. Click the link below to reset your password:</p>
                    <a href="$resetLink">Reset Password</a>
                    <p>If you did not request this, please ignore this email.</p>
                    <p>Best regards,<br/>${appModuleConfiguration.appName} Team</p>
                </body>
            </html>
            """.trimIndent()
    }

    fun generateWelcomeEmail(data: Map<WelcomeEmailLabel, String>): String {
        val username = data[WelcomeEmailLabel.USERNAME] ?: "User"

        return """
            <html>
                <body>
                    <p>Hi $username,</p>
                    <p>Welcome to ${appModuleConfiguration.appName}</p>
                    <p>Best regards,<br/>${appModuleConfiguration.appName} Team</p>
                </body>
            </html>
            """.trimIndent()
    }
}

enum class ResetPasswordMailLabel {
    USERNAME,
    RESET_LINK,
}

enum class WelcomeEmailLabel {
    USERNAME,
}
