package ktor.oauth2.multi.tenant.auth.server.security.plugins

import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.request.path
import io.ktor.server.request.receiveParameters
import io.ktor.util.AttributeKey

val AuthorizationCodeAttributeKey = AttributeKey<String>("authorization_code")

val authorizationCodeRequestPlugin: ApplicationPlugin<Unit> =
    createApplicationPlugin("AuthorizationCodeRequestPlugin") {
        onCall { call ->
            if (call.request.path().startsWith("/oauth/token")) {
                call.receiveParameters()["code"]?.let { code ->
                    call.attributes.put(AuthorizationCodeAttributeKey, code)
                }
            }
        }
    }
