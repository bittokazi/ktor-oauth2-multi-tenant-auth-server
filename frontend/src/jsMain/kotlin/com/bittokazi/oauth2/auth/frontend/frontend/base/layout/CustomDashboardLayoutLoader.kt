package com.bittokazi.oauth2.auth.frontend.frontend.base.layout

import com.bittokazi.kvision.spa.framework.base.layouts.LayoutLoader
import com.bittokazi.kvision.spa.framework.base.layouts.dashboard.layout.dashboardContent
import com.bittokazi.kvision.spa.framework.base.layouts.dashboard.layout.dashboardHeader
import com.bittokazi.kvision.spa.framework.base.layouts.dashboard.layout.dashboardTopBar
import io.kvision.core.Container
import io.kvision.html.Div
import io.kvision.html.div

class CustomDashboardLayoutLoader : LayoutLoader {
    override fun load(contentContainer: Container): Container {
        return Div(className = "wrapper") {
            dashboardHeader()
            div(className = "main") {
                dashboardTopBar()
                dashboardContent(contentContainer)
                customDashboardFooter()
            }
        }
    }
}
