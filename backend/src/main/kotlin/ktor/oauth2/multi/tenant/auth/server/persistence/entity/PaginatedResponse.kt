package ktor.oauth2.multi.tenant.auth.server.persistence.entity

data class PaginatedResponse<T>(
    var data: List<T>,
    val totalCount: Long,
    val totalPage: Long,
    var pages: List<Long> = listOf(),
    var prevPage: Long = 0,
    var nextPage: Long = 0,
    var isPrevPage: Boolean = false,
    var isNextPage: Boolean = false,
) {
    fun getAllPages(currentPage: Long) {
        val n = mutableListOf<Long>()
        for (i in currentPage..totalPage) {
            if (i < 5) {
                n.add(i)
            } else {
                break
            }
        }

        pages = n

        if (currentPage == 1L) {
            prevPage = 0
            isPrevPage = false
        } else {
            prevPage = currentPage - 1
            isPrevPage = true
        }

        if (currentPage == totalPage) {
            nextPage = 0
            isNextPage = false
        } else {
            nextPage = currentPage + 1
            isNextPage = true
        }
    }
}
