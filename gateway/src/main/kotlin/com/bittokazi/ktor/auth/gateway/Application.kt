package com.bittokazi.ktor.auth.gateway

import com.bittokazi.ktor.gateway.GatewayPlugin
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(GatewayPlugin) {
        sessionValidityInSeconds = 3600
        configureHttp = false
    }
}
