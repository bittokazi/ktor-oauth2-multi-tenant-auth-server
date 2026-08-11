package ktor.oauth2.multi.tenant.auth.server.security.config

import com.bittokazi.ktor.auth.services.issuer.IssuerProvider
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin

class IssuerProviderImpl : IssuerProvider {
    override fun getIssuer(call: ApplicationCall): String {
        val origin = call.request.origin
        return "https://${origin.serverHost}"
    }
}
