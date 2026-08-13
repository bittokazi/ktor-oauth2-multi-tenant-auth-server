package com.bittokazi.oauth2.auth.frontend.frontend.base.layout

import com.bittokazi.kvision.spa.framework.base.common.ObservableManager
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.services.SpaTenantService
import com.bittokazi.kvision.spa.framework.base.services.SpaTenantService.tenantInfoObserver
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.utils.Utils
import io.kvision.core.Container
import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.html.footer
import io.kvision.html.li
import io.kvision.html.link
import io.kvision.html.span
import io.kvision.html.strong
import io.kvision.html.ul
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import kotlin.collections.ifEmpty

@OptIn(ExperimentalSerializationApi::class)
fun Container.customDashboardFooter() {
    footer(className = "footer") {
        div(className = "container-fluid") {
            div(className = "row text-muted") {
                div(className = "col-6 text-start") {
                    link("", SpaAppEngine.APP_DASHBOARD_ROUTE, dataNavigo = true) {
                        add(strong {
                            content = SpaTenantService.tenantInfo.name
                        })
                    }
                    add(span {
                        content = "&nbsp; &copy;"
                        rich = true
                    })
                }
                div(className = "col-6 text-end") {
                    ul(className = "list-inline") {
                        li(className = "list-inline-item") {
                            link(SpaTenantService.tenantInfo.systemVersion, className = "text-muted")
                        }
                        li(className = "list-inline-item") {
                            span(
                                " |",
                                className = "text-muted"
                            )
                        }
                        li(className = "list-inline-item") {
                            ObservableManager.setSubscriber("customDashboardFooterVersionInfoObserver1") {
                                tenantInfoObserver.subscribe {
                                    if (it != null) {
                                        span(
                                            "Change Log",
                                            className = "cursor-pointer"
                                        ).onClick {
                                            val changeLogs = AppEngine.tenantService.changeLogs.ifEmpty { listOf("No changelog available") }
                                            sweetAlert.fire(
                                                Json.encodeToDynamic(
                                                    mapOf(
                                                        "title" to "Change log",
                                                        "html" to Utils.formatChangeLogHtml(changeLogs),
                                                        "icon" to "info",
                                                        "confirmButtonText" to "Close",
                                                        "width" to "600px"
                                                    )
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
