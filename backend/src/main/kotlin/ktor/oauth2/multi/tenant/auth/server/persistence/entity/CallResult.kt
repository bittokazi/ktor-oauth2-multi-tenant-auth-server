package ktor.oauth2.multi.tenant.auth.server.persistence.entity

sealed class CallResult<T, E> {
    data class Success<T, E>(
        val outcome: T,
    ) : CallResult<T, E>()

    data class Failure<T, E>(
        val errorCode: E,
        val errorMessage: String = "",
        val errorData: Map<String, List<String>>? = null,
        val cause: Throwable? = null,
    ) : CallResult<T, E>()
}
