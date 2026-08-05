package ktor.oauth2.multi.tenant.auth.server.security.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.bittokazi.ktor.auth.OauthUserSession
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.warrenstrange.googleauth.GoogleAuthenticator
import io.ktor.http.Cookie
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.UserData
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.UserTrustedDeviceData
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.UserTwoFaSecretData
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.UserRepository
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.UserTrustedDeviceRepository
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.UserTwoFaSecretRepository
import ktor.oauth2.multi.tenant.auth.server.security.entity.TwoFASecretPayload
import ktor.oauth2.multi.tenant.auth.server.security.entity.UserTwoFaSession
import ktor.oauth2.multi.tenant.auth.server.utils.Utils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

interface TwoFaService {
    fun generateSecret(call: ApplicationCall): CallResult<TwoFASecretPayload, TwoFaServiceError>

    fun enable2FA(
        twoFASecretPayload: TwoFASecretPayload,
        call: ApplicationCall,
    ): CallResult<UserData, TwoFaServiceError>

    fun disable(call: ApplicationCall): CallResult<UserData, TwoFaServiceError>

    fun verify(
        code: String,
        saveDevice: Boolean,
        call: ApplicationCall,
    ): CallResult<Unit, TwoFaServiceError>

    fun getTrustedDevices(call: ApplicationCall): CallResult<List<UserTrustedDeviceData>, TwoFaServiceError>

    fun deleteTrustedDevices(call: ApplicationCall): CallResult<UserTrustedDeviceData, TwoFaServiceError>

    fun generateScratchCodes(call: ApplicationCall): CallResult<List<String>, TwoFaServiceError>
}

class DefaultTwoFaService(
    val userRepository: UserRepository,
    val userTwoFaSecretRepository: UserTwoFaSecretRepository,
    val userTrustedDeviceRepository: UserTrustedDeviceRepository,
) : TwoFaService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[TwoFaService] -> init")
    }

    override fun generateSecret(call: ApplicationCall): CallResult<TwoFASecretPayload, TwoFaServiceError> {
        return call.principal<JWTPrincipal>()?.let { principal ->
            when (val user = userRepository.findOneById(principal.subject!!, false, call)) {
                is UserData -> {
                    when (user.twoFaEnabled) {
                        false -> {
                            val gAuth = GoogleAuthenticator()
                            val key = gAuth.createCredentials()

                            CallResult.Success(
                                TwoFASecretPayload(
                                    secret = key.key,
                                    scratchCodes = (0..4).map { Utils.numberGenerator(6) },
                                ),
                            )
                        }
                        else -> CallResult.Failure(TwoFaServiceError.TWO_FA_ALREADY_ENABLED)
                    }
                }

                else -> CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
            }
        } ?: CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
    }

    override fun enable2FA(
        twoFASecretPayload: TwoFASecretPayload,
        call: ApplicationCall,
    ): CallResult<UserData, TwoFaServiceError> {
        return call.principal<JWTPrincipal>()?.let { principal ->
            when (val user = userRepository.findOneById(principal.subject!!, false, call)) {
                is UserData -> {
                    when (user.id) {
                        is String -> {
                            when (user.twoFaEnabled) {
                                false -> {
                                    when (validate2FA(twoFASecretPayload.code!!, twoFASecretPayload.secret!!)) {
                                        true -> {
                                            userTwoFaSecretRepository.insert(
                                                UserTwoFaSecretData(
                                                    user = user,
                                                    secret = twoFASecretPayload.secret!!,
                                                    scratchCodes =
                                                        Gson().toJson(
                                                            twoFASecretPayload
                                                                .scratchCodes
                                                                .map { code ->
                                                                    BCrypt.withDefaults().hashToString(
                                                                        12,
                                                                        code.toCharArray(),
                                                                    )
                                                                }.toList(),
                                                        ),
                                                ),
                                                call,
                                            )
                                            val updated = userRepository.updateTwoFa(user.id, true, call)!!

                                            CallResult.Success(updated)
                                        }
                                        else -> CallResult.Failure(TwoFaServiceError.INVALID_CODE_OR_SECRET)
                                    }
                                }
                                else -> CallResult.Failure(TwoFaServiceError.TWO_FA_ALREADY_ENABLED)
                            }
                        }

                        else -> CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
                    }
                }

                else -> CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
            }
        } ?: CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
    }

    override fun disable(call: ApplicationCall): CallResult<UserData, TwoFaServiceError> {
        return call.principal<JWTPrincipal>()?.let { principal ->
            when (val user = userRepository.findOneById(principal.subject!!, false, call)) {
                is UserData -> {
                    when (user.id) {
                        is String -> {
                            when (user.twoFaEnabled) {
                                false -> CallResult.Failure(TwoFaServiceError.TWO_FA_NOT_ENABLED)

                                true -> {
                                    userTwoFaSecretRepository.delete(user.id, call)
                                    userTrustedDeviceRepository.deleteAllByUserId(user.id, call)

                                    val updated = userRepository.updateTwoFa(user.id, false, call)!!

                                    CallResult.Success(updated)
                                }
                            }
                        }

                        else -> CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
                    }
                }

                else -> CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
            }
        } ?: CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
    }

    override fun verify(
        code: String,
        saveDevice: Boolean,
        call: ApplicationCall,
    ): CallResult<Unit, TwoFaServiceError> {
        return when (val session = call.sessions.get<OauthUserSession>()) {
            null -> CallResult.Failure(TwoFaServiceError.NO_LOGIN)
            else ->
                when (val user = userRepository.findOneById(session.userId, false, call)) {
                    is UserData -> {
                        when (user.id) {
                            is String -> {
                                when (user.twoFaEnabled) {
                                    true -> {
                                        val twoFaSecretData = userTwoFaSecretRepository.findByUserId(user.id, call)
                                        when (
                                            validate2FA(code.toInt(), twoFaSecretData?.secret) ||
                                                validate2FAScratchCode(code, twoFaSecretData, call)
                                        ) {
                                            true -> {
                                                // Before returning success, ensure a device cookie exists and optionally persist trusted device
                                                // Accept either 'instanceId' or 'deviceId' cookie from client; prefer 'instanceId'.
                                                var instanceId = call.request.cookies["instanceId"]

                                                if (instanceId.isNullOrBlank()) {
                                                    // generate a new instanceId and set cookie(s) with long expiry (~10 years)
                                                    instanceId = UUID.randomUUID().toString()
                                                    val maxAge = 60 * 60 * 24 * 365 * 10 // 10 years in seconds
                                                    call.response.cookies.append(
                                                        Cookie("instanceId", instanceId, path = "/", maxAge = maxAge),
                                                    )
                                                }

                                                if (saveDevice) {
                                                    try {
                                                        val deviceIp =
                                                            call.request.headers["X-Forwarded-For"]
                                                                ?: call.request.headers["X-Real-IP"]
                                                        val userAgent = call.request.headers["User-Agent"]
                                                        userTrustedDeviceRepository.insert(
                                                            user.id,
                                                            instanceId,
                                                            deviceIp,
                                                            userAgent,
                                                            call,
                                                        )
                                                    } catch (e: Exception) {
                                                        log.warn("[TwoFaService] unable to save trusted device: ${e.message}")
                                                    }
                                                }

                                                val session = UserTwoFaSession(user.id, user.email)
                                                call.sessions.set(session)
                                                CallResult.Success(Unit)
                                            }

                                            else -> CallResult.Failure(TwoFaServiceError.INVALID_CODE_OR_SECRET)
                                        }
                                    }

                                    else -> CallResult.Failure(TwoFaServiceError.TWO_FA_NOT_ENABLED)
                                }
                            }

                            else -> CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
                        }
                    }

                    else -> CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
                }
        }
    }

    override fun getTrustedDevices(call: ApplicationCall): CallResult<List<UserTrustedDeviceData>, TwoFaServiceError> {
        return call.principal<JWTPrincipal>()?.let { principal ->
            when (val user = userRepository.findOneById(principal.subject!!, false, call)) {
                is UserData -> {
                    when (user.id) {
                        is String -> {
                            val devices = userTrustedDeviceRepository.findAllByUserId(user.id, call)
                            CallResult.Success(devices)
                        }

                        else -> CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
                    }
                }

                else -> CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
            }
        } ?: CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
    }

    override fun deleteTrustedDevices(call: ApplicationCall): CallResult<UserTrustedDeviceData, TwoFaServiceError> {
        return call.principal<JWTPrincipal>()?.let { principal ->
            call.parameters["id"]?.let { id ->
                when (val user = userRepository.findOneById(principal.subject!!, false, call)) {
                    is UserData -> {
                        when (user.id) {
                            is String -> {
                                when (val device = userTrustedDeviceRepository.findByUserIdAndId(user.id, id.toLong(), call)) {
                                    is UserTrustedDeviceData -> {
                                        userTrustedDeviceRepository.deleteByUserIdAndId(user.id, id.toLong(), call)
                                        CallResult.Success(device)
                                    }
                                    else -> CallResult.Failure(TwoFaServiceError.DEVICE_NOT_FOUND)
                                }
                            }

                            else -> CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
                        }
                    }

                    else -> CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
                }
            } ?: CallResult.Failure(TwoFaServiceError.DEVICE_NOT_FOUND)
        } ?: CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
    }

    override fun generateScratchCodes(call: ApplicationCall): CallResult<List<String>, TwoFaServiceError> {
        return call.principal<JWTPrincipal>()?.let { principal ->
            when (val user = userRepository.findOneById(principal.subject!!, false, call)) {
                is UserData -> {
                    when (user.id) {
                        is String -> {
                            when (user.twoFaEnabled) {
                                false -> CallResult.Failure(TwoFaServiceError.TWO_FA_NOT_ENABLED)

                                true -> {
                                    when (val twoFaSecretData = userTwoFaSecretRepository.findByUserId(user.id, call)) {
                                        is UserTwoFaSecretData -> {
                                            val scratchCodes = (0..4).map { Utils.numberGenerator(6) }
                                            twoFaSecretData.scratchCodes =
                                                Gson().toJson(
                                                    scratchCodes.map { code ->
                                                        BCrypt.withDefaults().hashToString(
                                                            12,
                                                            code.toCharArray(),
                                                        )
                                                    }.toList(),
                                                )
                                            userTwoFaSecretRepository.updateScratchCodes(twoFaSecretData, call)

                                            CallResult.Success(scratchCodes.map(String::toString))
                                        }
                                        else -> CallResult.Failure(TwoFaServiceError.TWO_FA_NOT_ENABLED)
                                    }
                                }
                            }
                        }

                        else -> CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
                    }
                }

                else -> CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
            }
        } ?: CallResult.Failure(TwoFaServiceError.USER_NOT_FOUND)
    }

    fun validate2FA(
        code: Int,
        secret: String?,
    ): Boolean {
        val gAuth = GoogleAuthenticator()
        return gAuth.authorize(secret, code)
    }

    fun validate2FAScratchCode(
        code: String,
        userTwoFaSecretData: UserTwoFaSecretData?,
        call: ApplicationCall,
    ): Boolean {
        userTwoFaSecretData?.let {
            val listType = object : TypeToken<ArrayList<String?>?>() {}.type
            val scratchCodes: MutableList<String>
            if (userTwoFaSecretData.scratchCodes != null) {
                scratchCodes = Gson().fromJson(userTwoFaSecretData.scratchCodes, listType)

                val tmp =
                    scratchCodes
                        .filter { scratchCode: String? ->
                            BCrypt.verifyer().verify(code.toCharArray(), scratchCode).verified
                        }
                if (tmp.isNotEmpty()) {
                    scratchCodes.removeAll(tmp)
                    userTwoFaSecretData.scratchCodes = Gson().toJson(scratchCodes)
                    userTwoFaSecretRepository.updateScratchCodes(userTwoFaSecretData, call)
                    return true
                }
            }
        }
        return false
    }
}

enum class TwoFaServiceError {
    USER_NOT_FOUND,
    INVALID_CODE_OR_SECRET,
    TWO_FA_ALREADY_ENABLED,
    TWO_FA_NOT_ENABLED,
    NO_LOGIN,
    DEVICE_NOT_FOUND,
}
