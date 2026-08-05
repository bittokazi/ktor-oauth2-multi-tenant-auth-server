package ktor.oauth2.multi.tenant.auth.server.security.entity

import ktor.oauth2.multi.tenant.auth.server.persistence.entity.RoleEnum
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.ScopeEnum

object RoleScopeConfiguration {
    val roles = mapOf(RoleEnum.ROLE_SUPER_ADMIN to ScopeEnum.entries.toSet())
}
