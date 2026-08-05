package ktor.oauth2.multi.tenant.auth.server.app.user.services

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ktor.oauth2.multi.tenant.auth.server.app.config.AppModuleConfiguration
import ktor.oauth2.multi.tenant.auth.server.config.AppConfig
import ktor.oauth2.multi.tenant.auth.server.mail.config.MailConfig
import ktor.oauth2.multi.tenant.auth.server.mail.services.MailGeneratorService
import ktor.oauth2.multi.tenant.auth.server.mail.services.MailService
import ktor.oauth2.multi.tenant.auth.server.mail.services.WelcomeEmailLabel
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PaginatedResponse
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PaginationRequest
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.UserData
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.UserRepository
import ktor.oauth2.multi.tenant.auth.server.utils.Utils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface UserService {
    fun whoAmI(call: ApplicationCall): CallResult<UserData, UserServiceError>

    fun register(
        call: ApplicationCall,
        userData: UserData,
        signUp: Boolean,
    ): CallResult<UserData, UserServiceError>

    fun get(call: ApplicationCall): CallResult<UserData, UserServiceError>

    fun getAll(
        paginationRequest: PaginationRequest,
        call: ApplicationCall,
    ): PaginatedResponse<UserData>

    fun update(
        call: ApplicationCall,
        userData: UserData,
        self: Boolean,
    ): CallResult<UserData, UserServiceError>

    fun updatePassword(
        call: ApplicationCall,
        userData: UserData,
        self: Boolean,
    ): CallResult<UserData, UserServiceError>

    fun findByProperty(call: ApplicationCall): CallResult<UserData, UserServiceError>
}

class DefaultUserService(
    val userRepository: UserRepository,
    val mailGeneratorService: MailGeneratorService,
    val mailService: MailService,
    val mailConfig: MailConfig,
    val appModuleConfiguration: AppModuleConfiguration,
) : UserService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[UserService] -> init")
    }

    override fun whoAmI(call: ApplicationCall): CallResult<UserData, UserServiceError> {
        return when (val principal = call.principal<JWTPrincipal>()) {
            is JWTPrincipal -> {
                principal.subject?.let { id ->
                    val userData =
                        userRepository.findOneById(
                            id,
                            false,
                            call.attributes[AttributeKey<String>(AppConfig.AUTH_TENANT_ATTRIBUTE_KEY)],
                        )?.apply {
                            adminTenantUser = Utils.isPublicTenant(call)
                        }

                    when (userData) {
                        is UserData -> CallResult.Success(userData)
                        else -> CallResult.Failure(UserServiceError.NOT_FOUND)
                    }
                } ?: CallResult.Failure(UserServiceError.NOT_FOUND)
            }
            else -> {
                CallResult.Failure(UserServiceError.NOT_FOUND)
            }
        }
    }

    override fun register(
        call: ApplicationCall,
        userData: UserData,
        signUp: Boolean,
    ): CallResult<UserData, UserServiceError> {
        return when (userRepository.findOneByEmail(userData.email, call = call)) {
            is UserData -> CallResult.Failure(UserServiceError.EMAIL_EXIST)

            else -> {
                val inserted = userRepository.insert(userData, call)

                if (call.attributes[AttributeKey<String>(AppConfig.TENANT_ATTRIBUTE_KEY)] == "public") {
                    CoroutineScope(Dispatchers.Default).launch {
                        mailService.sendMail(
                            inserted.email,
                            mailConfig.from,
                            "Welcome to ${appModuleConfiguration.appName}",
                            mailGeneratorService.generateWelcomeEmail(
                                mapOf(
                                    WelcomeEmailLabel.USERNAME
                                        to "${inserted.firstName} ${inserted.lastName}",
                                ),
                            ),
                        )
                    }
                }

                CallResult.Success(inserted)
            }
        }
    }

    override fun get(call: ApplicationCall): CallResult<UserData, UserServiceError> {
        return call.parameters["id"]?.let { id ->
            when (val existing = userRepository.findOneById(id, call = call)) {
                is UserData -> CallResult.Success(existing)

                else -> CallResult.Failure(UserServiceError.NOT_FOUND)
            }
        } ?: CallResult.Failure(UserServiceError.NOT_FOUND)
    }

    override fun getAll(
        paginationRequest: PaginationRequest,
        call: ApplicationCall,
    ): PaginatedResponse<UserData> {
        return userRepository.getAll(paginationRequest, call = call)
    }

    override fun update(
        call: ApplicationCall,
        userData: UserData,
        self: Boolean,
    ): CallResult<UserData, UserServiceError> {
        return when (userData.id) {
            is String -> {
                when (self) {
                    true -> updateOwnProfile(call, userData)

                    false -> {
                        when (val existing = userRepository.findOneById(userData.id, call = call)) {
                            is UserData -> {
                                when (existing.email == userData.email) {
                                    true ->
                                        CallResult.Success(
                                            userRepository.updateProfile(userData.id, userData, call = call)!!,
                                        )

                                    false ->
                                        when (userRepository.findOneByEmail(userData.email, call = call)) {
                                            is UserData -> CallResult.Failure(UserServiceError.EMAIL_EXIST)
                                            else ->
                                                CallResult.Success(
                                                    userRepository.updateProfile(userData.id, userData, call = call)!!,
                                                )
                                        }
                                }
                            }

                            else -> CallResult.Failure(UserServiceError.NOT_FOUND)
                        }
                    }
                }
            }

            else -> CallResult.Failure(UserServiceError.UNPROCESSABLE_ENTITY)
        }
    }

    override fun updatePassword(
        call: ApplicationCall,
        userData: UserData,
        self: Boolean,
    ): CallResult<UserData, UserServiceError> {
        return when (userData.id) {
            is String -> {
                when (self) {
                    true -> updateOwnPassword(call, userData)

                    false -> {
                        when (val existing = userRepository.findOneById(userData.id, true, call = call)) {
                            is UserData ->
                                CallResult.Success(
                                    userRepository.resetPassword(existing.id!!, userData.newPassword!!, call = call)!!,
                                )

                            else -> CallResult.Failure(UserServiceError.NOT_FOUND)
                        }
                    }
                }
            }

            else -> CallResult.Failure(UserServiceError.UNPROCESSABLE_ENTITY)
        }
    }

    override fun findByProperty(call: ApplicationCall): CallResult<UserData, UserServiceError> {
        return call.request.queryParameters["email"]?.let { email ->
            when (val existing = userRepository.findOneByEmail(email, call = call)) {
                is UserData -> CallResult.Success(existing)

                else -> CallResult.Failure(UserServiceError.NOT_FOUND)
            }
        } ?: CallResult.Failure(UserServiceError.NOT_FOUND)
    }

    private fun updateOwnProfile(
        call: ApplicationCall,
        userData: UserData,
    ): CallResult<UserData, UserServiceError> {
        return when (val principal = call.principal<JWTPrincipal>()) {
            is JWTPrincipal -> {
                principal.subject?.let { id ->
                    when (val existing = userRepository.findOneById(id, call = call)) {
                        is UserData ->
                            when (existing.id == userData.id) {
                                true ->
                                    CallResult.Success(
                                        userRepository.updateProfile(existing.id!!, userData, call = call)!!,
                                    )

                                false ->
                                    when (userRepository.findOneByEmail(userData.email, call = call)) {
                                        is UserData -> CallResult.Failure(UserServiceError.EMAIL_EXIST)
                                        else ->
                                            CallResult.Success(
                                                userRepository.updateProfile(existing.id!!, userData, call = call)!!,
                                            )
                                    }
                            }

                        else -> CallResult.Failure(UserServiceError.NOT_FOUND)
                    }
                } ?: CallResult.Failure(UserServiceError.NOT_FOUND)
            }

            else -> {
                CallResult.Failure(UserServiceError.NOT_FOUND)
            }
        }
    }

    private fun updateOwnPassword(
        call: ApplicationCall,
        userData: UserData,
    ): CallResult<UserData, UserServiceError> {
        return when (val principal = call.principal<JWTPrincipal>()) {
            is JWTPrincipal -> {
                principal.subject?.let { id ->
                    when (val existing = userRepository.findOneById(id, true, call = call)) {
                        is UserData ->
                            when (BCrypt.verifyer().verify(userData.password.toCharArray(), existing.password).verified) {
                                true ->
                                    CallResult.Success(
                                        userRepository.resetPassword(existing.id!!, userData.newPassword!!, call = call)!!,
                                    )

                                false -> CallResult.Failure(UserServiceError.PASSWORD_DOES_NOT_MATCH)
                            }

                        else -> CallResult.Failure(UserServiceError.NOT_FOUND)
                    }
                } ?: CallResult.Failure(UserServiceError.NOT_FOUND)
            }

            else -> {
                CallResult.Failure(UserServiceError.NOT_FOUND)
            }
        }
    }
}

enum class UserServiceError {
    NOT_FOUND,
    EMAIL_EXIST,
    UNPROCESSABLE_ENTITY,
    PASSWORD_DOES_NOT_MATCH,
}
