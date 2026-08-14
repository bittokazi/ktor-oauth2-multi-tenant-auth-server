package ktor.oauth2.multi.tenant.auth.server.app.client.services

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.server.application.ApplicationCall
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.ClientDto
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.ClientRepository
import ktor.oauth2.multi.tenant.auth.server.utils.Utils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

interface ClientService {
    fun save(
        oauthClientDTO: ClientDto,
        call: ApplicationCall,
    ): CallResult<ClientDto, ClientServiceError>

    fun all(call: ApplicationCall): List<ClientDto>

    fun get(
        id: String,
        call: ApplicationCall,
    ): CallResult<ClientDto, ClientServiceError>

    fun update(
        oauthClientDTO: ClientDto,
        isNewSecret: Boolean,
        call: ApplicationCall,
    ): CallResult<ClientDto, ClientServiceError>

    fun delete(
        id: String,
        call: ApplicationCall,
    ): CallResult<ClientDto, ClientServiceError>
}

class DefaultClientService(
    val clientRepository: ClientRepository,
) : ClientService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[ClientService] -> init")
    }

    override fun save(
        oauthClientDTO: ClientDto,
        call: ApplicationCall,
    ): CallResult<ClientDto, ClientServiceError> {
        val clientId = UUID.randomUUID()
        val newSecret = Utils.randomNumberGenerator(32)
        oauthClientDTO.clientId = clientId.toString()
        oauthClientDTO.clientSecret =
            BCrypt.withDefaults().hashToString(
                12,
                newSecret.toCharArray(),
            )

        return when (val result = clientRepository.save(oauthClientDTO = oauthClientDTO, call = call)) {
            is ClientDto -> {
                result.newSecret = newSecret
                CallResult.Success(result)
            }
            else -> CallResult.Failure(ClientServiceError.INSERT_ERROR)
        }
    }

    override fun all(call: ApplicationCall): List<ClientDto> = clientRepository.findAll(call = call)

    override fun get(
        id: String,
        call: ApplicationCall,
    ): CallResult<ClientDto, ClientServiceError> =
        when (
            val oauthClientDTO = clientRepository.findOneById(id, call = call)
        ) {
            is ClientDto -> CallResult.Success(oauthClientDTO)
            else -> CallResult.Failure(ClientServiceError.NOT_FOUND)
        }

    override fun update(
        oauthClientDTO: ClientDto,
        isNewSecret: Boolean,
        call: ApplicationCall,
    ): CallResult<ClientDto, ClientServiceError> =
        when (
            clientRepository.findOneById(id = oauthClientDTO.id!!, call = call)
        ) {
            null -> CallResult.Failure(ClientServiceError.NOT_FOUND)
            is ClientDto -> {
                val newSecret = Utils.randomNumberGenerator(32)
                if (isNewSecret) {
                    oauthClientDTO.clientSecret =
                        BCrypt.withDefaults().hashToString(
                            12,
                            newSecret.toCharArray(),
                        )
                }

                when (val result = clientRepository.update(oauthClientDTO = oauthClientDTO, call = call)) {
                    is ClientDto -> {
                        if (isNewSecret) {
                            result.newSecret = newSecret
                        }
                        CallResult.Success(result)
                    }
                    else -> CallResult.Failure(ClientServiceError.UPDATE_ERROR)
                }
            }
        }

    override fun delete(
        id: String,
        call: ApplicationCall,
    ): CallResult<ClientDto, ClientServiceError> =
        when (
            val existing = clientRepository.findOneById(id, call = call)
        ) {
            null -> CallResult.Failure(ClientServiceError.NOT_FOUND)
            is ClientDto -> {
                when (existing.isDefault) {
                    true -> CallResult.Failure(ClientServiceError.DEFAULT_DELETE_NOT_PERMITTED)
                    false ->
                        when (
                            val result = clientRepository.delete(id, call = call)
                        ) {
                            is ClientDto -> CallResult.Success(result)
                            else -> CallResult.Failure(ClientServiceError.DELETE_ERROR)
                        }
                }
            }
        }
}

enum class ClientServiceError {
    CLIENT_EXIST,
    INSERT_ERROR,
    UPDATE_ERROR,
    NOT_FOUND,
    DELETE_ERROR,
    DEFAULT_DELETE_NOT_PERMITTED,
}
