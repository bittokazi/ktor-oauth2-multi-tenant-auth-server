package com.bittokazi.oauth2.auth.frontend.frontend.base.services

import com.bittokazi.kvision.spa.framework.base.common.AuthData
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.common.AuthInformationProvider
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine.defaultAuthHolder
import com.bittokazi.kvision.spa.framework.base.models.RefreshTokenRequestProvider
import com.bittokazi.kvision.spa.framework.base.models.SpaRole
import com.bittokazi.kvision.spa.framework.base.models.SpaUser
import com.bittokazi.kvision.spa.framework.base.services.SpaAuthService
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.SweetAlert2
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.User
import io.kvision.rest.*
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToDynamic
import kotlinx.serialization.json.jsonPrimitive
import kotlin.js.Promise

class AuthService: AuthInformationProvider, RefreshTokenRequestProvider {

    var user: User? = null
    private val restService = SpaAppEngine.restService

    fun whoAmI(): Promise<RestResponse<User>> {
        return SpaAppEngine.restService.createAuthCall {
            restService.getClient().request<User>("${restService.BASE_URL}/api/v1/users/whoami") {
                method = HttpMethod.GET
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun getAuthProvider(): Promise<SpaUser> {
        return Promise { resolve, reject ->
            whoAmI().then {
                user = it.data
                resolve(
                    SpaUser(
                        id = it.data.id,
                        email = it.data.email,
                        firstName = it.data.firstName,
                        lastName = it.data.lastName,
                        avatarImage = it.data.image,
                        spaRoles = it.data.roles?.map { role ->
                            SpaRole(
                                title = role.name,
                                name = role.roleKey,
                                id = role.id
                            )
                        },
                        adminTenantUser = it.data.adminTenantUser,
                    )
                )
            }.catch { throwable ->
                if (throwable is RemoteRequestException) {
                    if (throwable.code.toInt() == 403) {
                        sweetAlert.fire(
                            Json.encodeToDynamic(
                                mapOf(
                                    "title" to "Access Forbidden",
                                    "confirmButtonText" to "Logout",
                                )
                            )
                        ).then {
                            SpaAppEngine.spaAuthService.logout(
                                oauth2LoginPage = false,
                                fullLogout = true
                            )
                        }
                        return@catch
                    }
                }
                reject(throwable)
            }
        }
    }

    override fun getRequest(): JsonObject {
        return JsonObject(
            mapOf(
                "refreshToken" to JsonPrimitive(defaultAuthHolder.getAuth()?.refreshToken ?: "")
            )
        )
    }

    override fun getAuthDataFromRefreshTokenResponse(response: JsonObject): AuthData {
        return AuthData(
            token = response["access_token"]?.jsonPrimitive?.content ?: "",
            refreshToken = response["refresh_token"]?.jsonPrimitive?.content ?: ""
        )
    }

    fun getCodeChallenge(): String? {
        localStorage.getItem("codeChallenge")?.let {
            if(it.isEmpty() || it.isBlank() || it == "null") return null
            return it
        }
        return null
    }

    fun setCodeChallenge(verifier: String?) {
        if (verifier == null) {
            localStorage.setItem("codeChallenge", "")
        } else {
            localStorage.setItem("codeChallenge", verifier)
        }
    }
}
