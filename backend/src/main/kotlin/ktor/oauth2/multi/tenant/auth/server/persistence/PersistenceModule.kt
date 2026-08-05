package ktor.oauth2.multi.tenant.auth.server.persistence

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.ClientRepository
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.PasswordResetTokenRepository
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.RoleRepository
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.TenantRepository
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.UserRepository
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.UserTrustedDeviceRepository
import ktor.oauth2.multi.tenant.auth.server.persistence.repository.UserTwoFaSecretRepository

fun Application.configurePersistenceModule() {
    dependencies {
        provide(UserRepository::class)
        provide(RoleRepository::class)
        provide(PasswordResetTokenRepository::class)
        provide(TenantRepository::class)
        provide(ClientRepository::class)
        provide(UserTwoFaSecretRepository::class)
        provide(UserTrustedDeviceRepository::class)
    }
}
