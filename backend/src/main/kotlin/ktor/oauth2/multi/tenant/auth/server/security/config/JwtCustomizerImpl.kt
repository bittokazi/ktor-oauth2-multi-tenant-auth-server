package ktor.oauth2.multi.tenant.auth.server.security.config

import com.bittokazi.ktor.auth.services.JwtTokenCustomizer
import com.bittokazi.ktor.auth.services.providers.OAuthClientDTO
import com.nimbusds.jwt.JWTClaimsSet
import io.ktor.server.application.ApplicationCall
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.RoleEnum
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.ScopeEnum
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.UserRepository
import ktor.oauth2.multi.tenant.auth.server.security.entity.RoleScopeConfiguration
import ktor.oauth2.multi.tenant.auth.server.utils.Utils
import kotlin.collections.joinToString

class JwtCustomizerImpl(
    private val userRepository: UserRepository,
    private val extraTokenClaimConfig: ExtraTokenClaimConfig,
) : JwtTokenCustomizer {
    override fun customize(
        user: String?,
        client: OAuthClientDTO?,
        claims: JWTClaimsSet.Builder,
        call: ApplicationCall?,
    ): Map<String, Any> {
        if (user != null) {
            val userDto = userRepository.findOneById(user, call = call!!)

            var scopes =
                userDto?.roles
                    ?.get(0)?.roleKey?.let { roleKey ->
                        RoleEnum
                            .entries
                            .filter { it.name == roleKey }
                            .singleOrNull()?.let {
                                RoleScopeConfiguration.roles[RoleEnum.valueOf(roleKey)]?.let {
                                    val tmpScopes = it.toMutableSet()

                                    if (Utils.isPublicTenant(call)) {
                                        tmpScopes.add(ScopeEnum.TENANT_ALL)
                                    }
                                    tmpScopes.joinToString(" ") { roleKey -> roleKey.name }
                                } ?: ""
                            } ?: ""
                    } ?: ""

            scopes =
                when (scopes != "") {
                    true -> "${claims.claims["scope"]} $scopes"
                    false -> claims.claims["scope"] as String
                }

            scopes
                .split(" ")
                .toSet()
                .joinToString(" ") { it }

            val payload =
                mutableMapOf<String, Any>(
                    "scope" to scopes,
                    "username" to userDto!!.email,
                )

            extraTokenClaimConfig.extraTokenClaims
                .filter { it.clientId == client?.clientId }
                .forEach {
                    payload +=
                        when (it.type) {
                            ExtraTokenClaimType.STRING -> {
                                (it.claimName to it.claimValue)
                            }

                            ExtraTokenClaimType.LIST -> {
                                (it.claimName to it.claimValue.split(","))
                            }
                        }
                }

            return payload
        }

        return emptyMap()
    }
}
