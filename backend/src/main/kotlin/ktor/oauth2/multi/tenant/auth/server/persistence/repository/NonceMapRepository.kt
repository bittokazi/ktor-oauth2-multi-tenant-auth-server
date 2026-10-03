package ktor.oauth2.multi.tenant.auth.server.persistence.repository

import io.ktor.server.application.ApplicationCall
import ktor.oauth2.multi.tenant.auth.server.database.config.MultiTenantDatabaseConfiguration
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.NonceMapEntity
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.NonceMaps
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll

class NonceMapRepository(
    multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : BaseRepository(multiTenantDatabaseConfiguration) {
    fun insert(
        nonceMapEntity: NonceMapEntity,
        call: ApplicationCall,
    ): NonceMapEntity =
        query(call = call) {
            NonceMaps.insert {
                it[code] = nonceMapEntity.code
                it[nonce] = nonceMapEntity.nonce
            }
            nonceMapEntity
        }

    fun findByCode(
        code: String,
        call: ApplicationCall,
    ): NonceMapEntity? =
        query(call = call) {
            NonceMaps.selectAll()
                .where { NonceMaps.code eq code }
                .singleOrNull()
                ?.let { row ->
                    NonceMapEntity(
                        code = row[NonceMaps.code],
                        nonce = row[NonceMaps.nonce],
                    )
                }
        }

    fun deleteByCode(
        code: String,
        call: ApplicationCall,
    ): Int =
        query(call = call) {
            NonceMaps.deleteWhere { NonceMaps.code eq code }
        }
}
