package ktor.oauth2.multi.tenant.auth.server.persistence.entity

interface DtoConverter<T> {
    fun toDto(): T
}
