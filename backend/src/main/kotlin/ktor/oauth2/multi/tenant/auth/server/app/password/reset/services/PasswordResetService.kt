package ktor.oauth2.multi.tenant.auth.server.app.password.reset.services

import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ktor.oauth2.multi.tenant.auth.server.app.config.AppModuleConfiguration
import ktor.oauth2.multi.tenant.auth.server.config.AppConfig
import ktor.oauth2.multi.tenant.auth.server.mail.config.MailConfig
import ktor.oauth2.multi.tenant.auth.server.mail.services.MailGeneratorService
import ktor.oauth2.multi.tenant.auth.server.mail.services.MailService
import ktor.oauth2.multi.tenant.auth.server.mail.services.ResetPasswordMailLabel
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PasswordResetTokenData
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.UserData
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.PasswordResetTokenRepository
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

interface PasswordResetService {
    fun create(
        passwordResetTokenRequest: PasswordResetTokenRequest,
        call: ApplicationCall,
    ): CallResult<PasswordResetTokenData, PasswordResetTokenError>

    fun find(
        token: String,
        call: ApplicationCall,
    ): CallResult<PasswordResetTokenData, PasswordResetTokenError>

    fun reset(
        token: String,
        passwordResetRequest: PasswordResetRequest,
        call: ApplicationCall,
    ): CallResult<Boolean, PasswordResetTokenError>
}

@OptIn(ExperimentalTime::class)
class DefaultPasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val mailService: MailService,
    private val mailGeneratorService: MailGeneratorService,
    private val appModuleConfiguration: AppModuleConfiguration,
    private val mailConfig: MailConfig,
) : PasswordResetService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[PasswordResetService] -> init")
    }

    override fun create(
        passwordResetTokenRequest: PasswordResetTokenRequest,
        call: ApplicationCall,
    ): CallResult<PasswordResetTokenData, PasswordResetTokenError> {
        if (call.attributes[AttributeKey<String>(AppConfig.TENANT_ATTRIBUTE_KEY)] != "public") {
            return CallResult.Failure(PasswordResetTokenError.TENANT_NOT_ALLOWED)
        }
        return when (val userData = userRepository.findOneByEmail(passwordResetTokenRequest.email, call = call)) {
            is UserData ->
                when (userData.id) {
                    is String -> {
                        val result = passwordResetTokenRepository.create(userData.id, call = call)

                        CoroutineScope(Dispatchers.Default).launch {
                            mailService.sendMail(
                                result.user.email,
                                mailConfig.from,
                                "${appModuleConfiguration.appName} - Password Reset Request",
                                mailGeneratorService.generateResetPasswordMail(
                                    mapOf(
                                        ResetPasswordMailLabel.USERNAME to
                                            "${result.user.firstName} ${result.user.lastName}",
                                        ResetPasswordMailLabel.RESET_LINK to
                                            "${appModuleConfiguration.domain}/app/reset-password/${result.token}",
                                    ),
                                ),
                            )
                        }
                        CallResult.Success(result)
                    }

                    else -> CallResult.Failure(PasswordResetTokenError.USER_NOT_FOUND)
                }

            else -> CallResult.Failure(PasswordResetTokenError.USER_NOT_FOUND)
        }
    }

    override fun find(
        token: String,
        call: ApplicationCall,
    ): CallResult<PasswordResetTokenData, PasswordResetTokenError> {
        if (call.attributes[AttributeKey<String>(AppConfig.TENANT_ATTRIBUTE_KEY)] != "public") {
            return CallResult.Failure(PasswordResetTokenError.TENANT_NOT_ALLOWED)
        }

        return when (val passwordResetTokenData = passwordResetTokenRepository.findByToken(token, call = call)) {
            is PasswordResetTokenData ->
                when (passwordResetTokenData.expireDate < Clock.System.now()) {
                    false -> CallResult.Success(passwordResetTokenData)

                    true -> CallResult.Failure(PasswordResetTokenError.TOKEN_EXPIRED)
                }

            else -> CallResult.Failure(PasswordResetTokenError.TOKEN_NOT_FOUND)
        }
    }

    override fun reset(
        token: String,
        passwordResetRequest: PasswordResetRequest,
        call: ApplicationCall,
    ): CallResult<Boolean, PasswordResetTokenError> {
        if (call.attributes[AttributeKey<String>(AppConfig.TENANT_ATTRIBUTE_KEY)] != "public") {
            return CallResult.Failure(PasswordResetTokenError.TENANT_NOT_ALLOWED)
        }

        return when (val passwordResetTokenData = passwordResetTokenRepository.findByToken(token, call = call)) {
            is PasswordResetTokenData ->
                when (passwordResetTokenData.expireDate < Clock.System.now()) {
                    false ->
                        when (passwordResetRequest.password == passwordResetRequest.reTypedPassword) {
                            true ->
                                when (
                                    val user =
                                        userRepository.resetPassword(
                                            passwordResetTokenData.user.id!!,
                                            passwordResetRequest.password,
                                            call = call,
                                        )
                                ) {
                                    is UserData -> {
                                        passwordResetTokenRepository.delete(user, call = call)
                                        CallResult.Success(true)
                                    }

                                    else -> CallResult.Failure(PasswordResetTokenError.INTERNAL_ERROR)
                                }

                            else -> CallResult.Failure(PasswordResetTokenError.PASSWORD_MISMATCH)
                        }

                    true -> CallResult.Failure(PasswordResetTokenError.TOKEN_EXPIRED)
                }

            else -> CallResult.Failure(PasswordResetTokenError.TOKEN_NOT_FOUND)
        }
    }
}

enum class PasswordResetTokenError {
    USER_NOT_FOUND,
    TOKEN_NOT_FOUND,
    TOKEN_EXPIRED,
    PASSWORD_MISMATCH,
    INTERNAL_ERROR,
    TENANT_NOT_ALLOWED,
}

@Serializable
data class PasswordResetTokenRequest(
    val email: String,
)

@Serializable
data class PasswordResetRequest(
    val password: String,
    val reTypedPassword: String,
)
