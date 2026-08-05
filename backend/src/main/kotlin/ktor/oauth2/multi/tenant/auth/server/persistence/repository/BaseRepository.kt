package ktor.oauth2.multi.tenant.auth.server.persistence.repository

import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey
import ktor.oauth2.multi.tenant.auth.server.config.AppConfig
import ktor.oauth2.multi.tenant.auth.server.database.config.MultiTenantDatabaseConfiguration
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.DtoConverter
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.PaginatedResponse
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

open class BaseRepository(
    private val multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) {
    fun <T> query(
        identifier: String = "default",
        call: ApplicationCall?,
        block: () -> T,
    ): T {
        val schema = AttributeKey<String>(AppConfig.TENANT_ATTRIBUTE_KEY)
        return query(identifier, call!!.attributes[schema], block)
    }

    fun <T> query(
        identifier: String = "default",
        schema: String = "public",
        block: () -> T,
    ): T {
        return transaction(multiTenantDatabaseConfiguration.getTenantDatabase(identifier, schema)) {
            addLogger(StdOutSqlLogger)
            block()
        }
    }

    fun <T, E> getPaginatedResponse(
        iterable: SizedIterable<T>,
        limit: Long,
        page: Long,
        fn: (SizedIterable<T>) -> SizedIterable<T>,
    ): PaginatedResponse<E> where T : DtoConverter<E> {
        // 1. Calculate the total matching count before slicing
        val totalCount = iterable.count()

        // 2. Safe calculation of total pages (ensuring at least 1 page if empty)
        val totalPages = if (totalCount == 0L) 1L else (totalCount + limit - 1) / limit

        // 3. Prevent negative offsets if someone passes page <= 0
        val sanitizedPage = page.coerceAtLeast(1L)
        val offset = (sanitizedPage - 1) * limit

        // 4. CRITICAL FIX: Apply ordering (fn) FIRST, then apply pagination limits
        val orderedIterable = fn(iterable)
        val paginatedData =
            orderedIterable
                .limit(limit.toInt())
                .offset(offset)
                .toList()
                .map { it.toDto() }

        val paginationResponse =
            PaginatedResponse(
                totalCount = totalCount,
                totalPage = totalPages,
                data = paginatedData,
            )

        // Keep your side-effect logic if needed (e.g., population/logging hooks)
        paginationResponse.getAllPages(sanitizedPage)

        return paginationResponse
    }
}
