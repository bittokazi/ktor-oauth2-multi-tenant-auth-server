package com.bittokazi.oauth2.auth.frontend.frontend.base.common

import kotlin.js.Promise

@JsModule("kotlin-kvision-spa-framework-resources/static/js/sweetalert2.js")
external object SweetAlert2 {
    fun fire(
        options: dynamic
    ): Promise<dynamic>
}
