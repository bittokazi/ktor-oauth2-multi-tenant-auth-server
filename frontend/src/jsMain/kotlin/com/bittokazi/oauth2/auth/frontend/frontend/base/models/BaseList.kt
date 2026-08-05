package com.bittokazi.oauth2.auth.frontend.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
open class BaseList<T>(
    val data: List<T>,
    val totalCount: Long,
    val totalPage: Long,
    var pages: List<Long> = listOf(),
    var prevPage: Long = 0,
    var nextPage: Long = 0,
    var isPrevPage: Boolean = false,
    var isNextPage: Boolean = false,
)
