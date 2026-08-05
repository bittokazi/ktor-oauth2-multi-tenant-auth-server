package ktor.oauth2.multi.tenant.auth.server.persistence.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey
import ktor.oauth2.multi.tenant.auth.server.config.AppConfig
import ktor.oauth2.multi.tenant.auth.server.database.config.MultiTenantDatabaseConfiguration
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PaginatedResponse
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PaginationRequest
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.Roles
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.User
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.UserData
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.UserRoles
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.Users
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.Users.hashedPassword
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.upsert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class UserRepository(
    multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : BaseRepository(multiTenantDatabaseConfiguration) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[UserRepository] -> created")
    }

    fun findOneByEmail(
        email: String,
        password: Boolean = false,
        call: ApplicationCall,
    ): UserData? {
        return query(call = call) {
            User.find {
                Users.email eq email
            }.singleOrNull()?.toData(password) ?: run { null }
        }
    }

    fun findOneByEmail(
        tenant: String,
        email: String,
        call: ApplicationCall,
    ): UserData? {
        return query(call = call) {
            User.find {
                Users.email eq email
            }.singleOrNull()?.toData() ?: run { null }
        }
    }

    fun findOneById(
        id: String,
        password: Boolean = false,
        call: ApplicationCall,
    ): UserData? {
        return findOneById(
            id = id,
            password = password,
            schema = call.attributes[AttributeKey<String>(AppConfig.TENANT_ATTRIBUTE_KEY)],
        )
    }

    fun findOneById(
        id: String,
        password: Boolean = false,
        schema: String,
    ): UserData? {
        return query(schema = schema) {
            User.find {
                Users.id eq id
            }.singleOrNull()?.toData(password) ?: run { null }
        }
    }

    fun getAll(
        paginationRequest: PaginationRequest,
        call: ApplicationCall,
    ): PaginatedResponse<UserData> {
        return query(call = call) {
            // 1. Start with the base query
            var q = Users.innerJoin(UserRoles).innerJoin(Roles).select(Users.columns)

            var condition: Op<Boolean>? = null

            log.info("Processing query: ${paginationRequest.query}")

            paginationRequest.query
                ?.split(" ")
                ?.forEach { pair ->
                    val parts = pair.split("=", limit = 2)
                    if (parts.size != 2) return@forEach

                    log.info("Processing query part: ${parts[0]} = ${parts[1]}")

                    val op =
                        when (parts[0]) {
                            "email" -> Users.email like "%${parts[1]}%"
                            "firstName" -> Users.firstName like "%${parts[1]}%"
                            "lastName" -> Users.lastName like "%${parts[1]}%"
                            "role" -> Roles.roleKey eq parts[1]
                            else -> null
                        }

                    if (op != null) {
                        condition = condition?.and(op) ?: op
                    }
                }

            condition?.let {
                q = q.andWhere { it }
            }

            q = q.withDistinct()

            // 3. Handle pagination (Ensure 'User' implements DtoConverter)
            val limit = paginationRequest.count?.toLong() ?: 1L
            val page = paginationRequest.page?.toLong() ?: 1L

            getPaginatedResponse(
                User.wrapRows(q),
                limit,
                page,
            ) {
                // Apply sorting inside the configuration block if your helper supports it
                it.orderBy(Users.id to SortOrder.DESC)
            }
        }
    }

    fun insert(
        userData: UserData,
        call: ApplicationCall,
    ): UserData {
        return query(call = call) {
            val newUser =
                User.new {
                    email = userData.email
                    firstName = userData.firstName
                    lastName = userData.lastName
                    userData.password.let {
                        hashedPassword = BCrypt.withDefaults().hashToString(12, it.toCharArray())
                    }
                }.toData()

            if (newUser.id != null) {
                userData.roles?.let { roleDtos ->
                    roleDtos.forEach { roleDto ->
                        UserRoles.upsert {
                            it[userId] = newUser.id.let { it1 -> User.findById(it1)!!.id }
                            it[roleId] = roleDto.id!!
                        }
                    }
                }
            }
            newUser
        }
    }

    fun updateProfile(
        id: String,
        userData: UserData,
        call: ApplicationCall,
    ): UserData? {
        return query(call = call) {
            User.findByIdAndUpdate(id) { user ->
                user.firstName = userData.firstName
                user.lastName = userData.lastName
                user.email = userData.email
            }?.toData() ?: run { null }
        }
    }

    fun resetPassword(
        id: String,
        password: String,
        call: ApplicationCall,
    ): UserData? {
        return query(call = call) {
            User.findByIdAndUpdate(id) { user ->
                user.hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
            }?.toData()
        }
    }

    fun updateTwoFa(
        id: String,
        enabled: Boolean,
        call: ApplicationCall,
    ): UserData? {
        return query(call = call) {
            User.findByIdAndUpdate(id) { user ->
                user.twoFaEnabled = enabled
            }?.toData()
        }
    }
}
