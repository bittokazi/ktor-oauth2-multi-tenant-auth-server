package com.bittokazi.oauth2.auth.frontend.frontend.app

import com.bittokazi.kvision.spa.framework.base.common.RouterConfiguration
import com.bittokazi.kvision.spa.framework.base.common.module.ApplicationModule
import com.bittokazi.kvision.spa.framework.base.common.module.DefaultRootApplicationModule
import com.bittokazi.oauth2.auth.frontend.frontend.app.public.HomePage
import com.bittokazi.oauth2.auth.frontend.frontend.app.public.reset.password.ResetPasswordPage
import com.bittokazi.oauth2.auth.frontend.frontend.app.public.reset.password.ResetPasswordRequestPage
import com.bittokazi.oauth2.auth.frontend.frontend.app.public.signin.LoginPage
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboardModule
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine

fun rootModule(): ApplicationModule = DefaultRootApplicationModule(
    loginPage = {
        LoginPage()
    },
    securedModule = dashboardModule(),
    authInformationProvider = AppEngine.authService,
    RouterConfiguration(
        route = AppEngine.APP_BASE_ROUTE,
        title = "Home",
        view = {
            HomePage()
        }
    ),
    RouterConfiguration(
        route = "/app/reset-password",
        title = "AuthKit | Request Reset Password",
        view = {
            ResetPasswordRequestPage()
        },
    ),
    RouterConfiguration(
        route = "/app/reset-password/:token",
        title = "AuthKit | Reset Password",
        view = {
            ResetPasswordPage(it.data.token)
        },
    ),
)
