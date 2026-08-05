package ktor.oauth2.multi.tenant.auth.server.mail.services

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import ktor.oauth2.multi.tenant.auth.server.mail.config.MailConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Properties

interface MailService {
    fun sendMail(
        to: String,
        from: String,
        subject: String,
        body: String,
    )
}

class DefaultMailService(
    private val config: MailConfig,
) : MailService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[MailService] -> init")
    }

    override fun sendMail(
        to: String,
        from: String,
        subject: String,
        body: String,
    ) {
        val props =
            Properties().apply {
                put("mail.smtp.host", config.host)
                put("mail.smtp.port", config.port.toString())
                put("mail.smtp.auth", config.auth.toString())
                put("mail.smtp.starttls.enable", config.tls.toString())
            }

        val session =
            Session.getInstance(
                props,
                object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(config.username, config.password)
                    }
                },
            )

        val message =
            MimeMessage(session).apply {
                setFrom(InternetAddress(from))
                setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to),
                )
                setSubject(subject)
                setContent(body, "text/html; charset=utf-8")
            }

        try {
            Transport.send(message)
            log.info("Email sent successfully to $to")
        } catch (e: Exception) {
            log.error("Failed to send email to $to", e)
        }
    }
}

class NoopMailService : MailService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[NoopMailService] -> init")
    }

    override fun sendMail(
        to: String,
        from: String,
        subject: String,
        body: String,
    ) {
        log.warn("Email sending is disabled. No email will be sent to $to with subject '$subject'")
    }
}
