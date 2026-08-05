package ktor.oauth2.multi.tenant.auth.server.health.services

import io.ktor.server.application.*
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.CallResult

interface HealthService {
    fun checkHealth(call: ApplicationCall): CallResult<HealthCheckResponse, HealthServiceErrorCode>
}

data class HealthCheckResponse(
    val status: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
)

enum class HealthServiceErrorCode {
    DATABASE_UNAVAILABLE,
    HEALTH_CHECK_FAILED,
}
