package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.UserAddComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.UserListComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.UserUpdateComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.kvision.spa.framework.base.common.RouterConfiguration
import com.bittokazi.kvision.spa.framework.base.common.module.DefaultSecuredPageModule

fun userModule() =
    DefaultSecuredPageModule(
        RouterConfiguration(
            route = AppEngine.APP_DASHBOARD_USER_ADD_ROUTE,
            title = "Add User",
            view = {
                UserAddComponent()
            },
        ),
        RouterConfiguration(
            route = AppEngine.APP_DASHBOARD_USER_ROUTE,
            title = "All Users",
            view = {
                UserListComponent()
            },
        ),
        RouterConfiguration(
            route = AppEngine.APP_DASHBOARD_USER_UPDATE_ROUTE(":id"),
            title = "Update User",
            view = {
                UserUpdateComponent(it.data.id)
            },
        ),
    )
