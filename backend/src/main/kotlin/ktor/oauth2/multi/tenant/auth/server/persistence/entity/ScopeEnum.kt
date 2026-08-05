package ktor.oauth2.multi.tenant.auth.server.persistence.entity

enum class ScopeEnum {
    ROLE_SUPER_ADMIN,

    // User
    USER_WHO_AM_I,
    USER_READ_ALL,
    USER_READ,
    USER_ADD,
    USER_UPDATE,

    // Role
    ROLE_READ_ALL,
    ROLE_READ,
    ROLE_ADD,
    ROLE_UPDATE,

    // Client
    CLIENT_READ_ALL,
    CLIENT_READ,
    CLIENT_ADD,
    CLIENT_UPDATE,
    CLIENT_REMOVE,

    // Tenants
    TENANT_ALL,

    // Two FA
    USER_TWO_FA_ALL,
}
