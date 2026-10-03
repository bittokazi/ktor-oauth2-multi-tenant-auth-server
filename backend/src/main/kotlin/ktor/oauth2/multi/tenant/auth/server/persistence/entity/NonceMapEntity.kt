package ktor.oauth2.multi.tenant.auth.server.persistence.entity

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table

data class NonceMapEntity(
    val code: String,
    val nonce: String,
)

object NonceMaps : Table("nonce_map") {
    val code: Column<String> = varchar("code", length = 255)
    val nonce: Column<String> = varchar("nonce", length = 255)

    override val primaryKey = PrimaryKey(code)
}
