package ktor.oauth2.multi.tenant.auth.server.database.migration

import at.favre.lib.crypto.bcrypt.BCrypt
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.util.UUID

class V4__seed_role_user : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        if (context == null) throw RuntimeException()
        val schema = context.connection.schema
        val password = "password"
        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        val userId = UUID.randomUUID()
        val roleId = UUID.randomUUID()
        context.connection.createStatement().use { insert ->
            insert.execute(
                "INSERT INTO $schema.\"user\"\n" +
                    "(id, email, first_name, last_name, \"password\")\n" +
                    "VALUES('$userId', 'admin@example.com', 'Super', 'Admin', '$hashedPassword');",
            )
        }
        context.connection.createStatement().use { insert ->
            insert.execute(
                "INSERT INTO $schema.\"role\"\n" +
                    "(id, name, role_key)\n" +
                    "VALUES('$roleId', 'Super Admin', 'ROLE_SUPER_ADMIN');",
            )
        }
        context.connection.createStatement().use { insert ->
            insert.execute(
                "INSERT INTO $schema.\"user_role\"\n" +
                    "(user_id, role_id)\n" +
                    "VALUES('$userId', '$roleId');",
            )
        }
    }
}
