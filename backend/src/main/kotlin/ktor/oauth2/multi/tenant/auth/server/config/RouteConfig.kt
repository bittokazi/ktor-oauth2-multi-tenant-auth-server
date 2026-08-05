package ktor.oauth2.multi.tenant.auth.server.config

object RouteConfig {
    const val API = "/api"
    const val V1 = "v1"
    const val API_PUBLIC = "/api/public"

    // OAuth API
    const val LOGIN_API = "$API/$V1/oauth/login"
    const val REFRESH_TOKEN_API = "$API/$V1/oauth/refresh/token"
    const val OAUTH_CALLBACK_API = "$API/$V1/oauth/callback"

    // User API
    const val API_USER = "$API/$V1/users"
    const val API_USER_BY_ID = "$API_USER/{id}"
    const val API_USER_WHO_AM_I = "$API_USER/whoami"
    const val API_USER_WHO_AM_I_UPDATE_PASSWORD = "$API_USER/whoami/update-password"
    const val API_USER_UPDATE_PASSWORD = "$API_USER_BY_ID/update-password"
    const val API_USER_SEARCH = "$API_USER/search"

    // Client API
    const val API_CLIENT = "$API/$V1/clients"
    const val API_CLIENT_BY_ID = "$API_CLIENT/{id}"

    // Role API
    const val API_ROLE = "$API/$V1/roles"
    const val API_ROLE_BY_ID = "$API_ROLE/{id}"

    // Tenant API
    const val API_TENANT = "$API/$V1/tenants"
    const val API_TENANT_BY_ID = "$API_TENANT/{id}"
    const val API_TENANT_INFO = "$API/$V1/tenants/info"
    const val API_TENANT_TEMPLATE_UPLOAD = "$API_TENANT_BY_ID/templates"

    // Two Fa
    const val API_TWO_FA_ENABLE = "$API_USER_WHO_AM_I/mfa/enable"
    const val API_TWO_FA_DISABLE = "$API_USER_WHO_AM_I/mfa/disable"
    const val API_TWO_FA_SECRET = "$API_USER_WHO_AM_I/mfa/generate-secret"
    const val API_TWO_FA_SCRATCH_CODE = "$API_USER_WHO_AM_I/mfa/generate-scratch-codes"

    // Trusted Devices
    const val API_TRUSTED_DEVICES = "$API_USER_WHO_AM_I/mfa/trusted-devices"
    const val API_TRUSTED_DEVICE_BY_INSTANCE_ID = "$API_TRUSTED_DEVICES/{id}"
}
