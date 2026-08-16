package ktor.oauth2.multi.tenant.auth.server.persistence.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.bittokazi.ktor.auth.services.providers.database.OAuthClients
import io.ktor.server.application.ApplicationCall
import ktor.oauth2.multi.tenant.auth.server.database.config.MultiTenantDatabaseConfiguration
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.ClientDto
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteIgnoreWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import kotlin.text.split
import kotlin.text.toCharArray
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
class ClientRepository(
    multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : BaseRepository(multiTenantDatabaseConfiguration) {
    fun findOneById(
        id: String,
        call: ApplicationCall,
    ): ClientDto? =
        query(call = call) {
            OAuthClients.selectAll().where { OAuthClients.id eq UUID.fromString(id).toKotlinUuid() }
                .map {
                    ClientDto(
                        it[OAuthClients.id].toString(),
                        it[OAuthClients.clientId],
                        it[OAuthClients.clientName],
                        it[OAuthClients.clientType],
                        it[OAuthClients.redirectUris].split(","),
                        it[OAuthClients.scopes].split(","),
                        it[OAuthClients.grantTypes].split(","),
                        it[OAuthClients.clientSecret],
                        accessTokenValidity = it[OAuthClients.accessTokenValidity],
                        refreshTokenValidity = it[OAuthClients.refreshTokenValidity],
                        isDefault = it[OAuthClients.isDefault],
                        consentRequired = it[OAuthClients.consentRequired],
                        postLogoutRedirectUri = it[OAuthClients.postLogoutRedirectUri],
                    )
                }.singleOrNull()
        }

    fun findOneByClientId(
        clientId: String,
        call: ApplicationCall,
    ): ClientDto? =
        query(call = call) {
            OAuthClients.selectAll().where { OAuthClients.clientId eq clientId }
                .map {
                    ClientDto(
                        it[OAuthClients.id].toString(),
                        it[OAuthClients.clientId],
                        it[OAuthClients.clientName],
                        it[OAuthClients.clientType],
                        it[OAuthClients.redirectUris].split(","),
                        it[OAuthClients.scopes].split(","),
                        it[OAuthClients.grantTypes].split(","),
                        it[OAuthClients.clientSecret],
                        accessTokenValidity = it[OAuthClients.accessTokenValidity],
                        refreshTokenValidity = it[OAuthClients.refreshTokenValidity],
                        isDefault = it[OAuthClients.isDefault],
                        consentRequired = it[OAuthClients.consentRequired],
                        postLogoutRedirectUri = it[OAuthClients.postLogoutRedirectUri],
                    )
                }.singleOrNull()
        }

    fun save(
        oauthClientDTO: ClientDto,
        call: ApplicationCall,
    ): ClientDto? {
        return query(call = call) {
            val id = UUID.randomUUID().toKotlinUuid()
            val inserted =
                OAuthClients.insert {
                    it[OAuthClients.id] = id
                    it[OAuthClients.clientId] = oauthClientDTO.clientId!!
                    it[OAuthClients.clientSecret] =
                        BCrypt.withDefaults().hashToString(
                            12,
                            oauthClientDTO.clientSecret!!.toCharArray(),
                        )
                    it[OAuthClients.clientName] = oauthClientDTO.clientName
                    it[OAuthClients.clientType] = oauthClientDTO.clientType!!
                    it[OAuthClients.redirectUris] = oauthClientDTO.redirectUris.joinToString(",")
                    it[OAuthClients.scopes] = oauthClientDTO.scopes.joinToString(",")
                    it[OAuthClients.grantTypes] = oauthClientDTO.grantTypes.joinToString(",")
                    it[OAuthClients.tokenEndpointAuthMethod] = "client_secret_post"
                    it[OAuthClients.accessTokenValidity] = oauthClientDTO.accessTokenValidity
                    it[OAuthClients.refreshTokenValidity] = oauthClientDTO.refreshTokenValidity
                    it[OAuthClients.isDefault] = false
                    it[OAuthClients.consentRequired] = oauthClientDTO.consentRequired
                    when (oauthClientDTO.postLogoutRedirectUri.isNullOrBlank()) {
                        true -> it[OAuthClients.postLogoutRedirectUri] = null
                        false -> it[OAuthClients.postLogoutRedirectUri] = oauthClientDTO.postLogoutRedirectUri
                    }
                }
            if (inserted.insertedCount > 0) {
                findOneById(id.toString(), call)
            } else {
                null
            }
        }
    }

    fun findAll(call: ApplicationCall): List<ClientDto> =
        query(call = call) {
            OAuthClients.selectAll()
                .map {
                    ClientDto(
                        it[OAuthClients.id].toString(),
                        it[OAuthClients.clientId],
                        it[OAuthClients.clientName],
                        it[OAuthClients.clientType],
                        it[OAuthClients.redirectUris].split(","),
                        it[OAuthClients.scopes].split(","),
                        it[OAuthClients.grantTypes].split(","),
                        it[OAuthClients.clientSecret],
                        accessTokenValidity = it[OAuthClients.accessTokenValidity],
                        refreshTokenValidity = it[OAuthClients.refreshTokenValidity],
                        isDefault = it[OAuthClients.isDefault],
                        consentRequired = it[OAuthClients.consentRequired],
                        postLogoutRedirectUri = it[OAuthClients.postLogoutRedirectUri],
                    )
                }.toList()
        }

    fun update(
        oauthClientDTO: ClientDto,
        call: ApplicationCall,
    ): ClientDto? =
        query(call = call) {
            val updated =
                OAuthClients.update(
                    {
                        OAuthClients.id eq UUID.fromString(oauthClientDTO.id).toKotlinUuid()
                    },
                ) {
                    it[OAuthClients.clientId] = oauthClientDTO.clientId!!
                    it[OAuthClients.clientName] = oauthClientDTO.clientName
                    it[OAuthClients.clientType] = oauthClientDTO.clientType!!
                    it[OAuthClients.redirectUris] = oauthClientDTO.redirectUris.joinToString(",")
                    it[OAuthClients.scopes] = oauthClientDTO.scopes.joinToString(",")
                    it[OAuthClients.grantTypes] = oauthClientDTO.grantTypes.joinToString(",")
                    it[tokenEndpointAuthMethod] = "client_secret_post"
                    it[OAuthClients.accessTokenValidity] = oauthClientDTO.accessTokenValidity
                    it[OAuthClients.refreshTokenValidity] = oauthClientDTO.refreshTokenValidity
                    it[OAuthClients.consentRequired] = oauthClientDTO.consentRequired
                    if (oauthClientDTO.clientSecret != null) {
                        it[OAuthClients.clientSecret] =
                            BCrypt.withDefaults().hashToString(
                                12,
                                oauthClientDTO.clientSecret!!.toCharArray(),
                            )
                    }
                    when (oauthClientDTO.postLogoutRedirectUri.isNullOrBlank()) {
                        true -> it[OAuthClients.postLogoutRedirectUri] = null
                        false -> it[OAuthClients.postLogoutRedirectUri] = oauthClientDTO.postLogoutRedirectUri
                    }
                }

            if (updated > 0) {
                findOneById(oauthClientDTO.id.toString(), call)
            } else {
                null
            }
        }

    fun delete(
        id: String,
        call: ApplicationCall,
    ): ClientDto? =
        query(call = call) {
            when (val oauthClientDTO = findOneById(id, call)) {
                is ClientDto -> {
                    val deleted =
                        OAuthClients.deleteIgnoreWhere {
                            OAuthClients.id eq UUID.fromString(id).toKotlinUuid()
                        }
                    if (deleted > 0) {
                        findOneById(oauthClientDTO.id.toString(), call)
                    } else {
                        null
                    }
                }

                else -> null
            }
        }
}
