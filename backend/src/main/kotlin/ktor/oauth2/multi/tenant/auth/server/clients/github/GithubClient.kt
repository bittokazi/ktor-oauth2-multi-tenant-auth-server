package ktor.oauth2.multi.tenant.auth.server.clients.github

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.GitHubChangeLogDto
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.GithubChangeLogEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GithubClient {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private val applicationHttpClient: HttpClient =
        HttpClient(Apache) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
        }

    init {
        log.info("[GithubClient] -> created")
    }

    suspend fun getReleaseChangeLog(tag: String): GithubChangeLogEntity? {
        val url = "https://api.github.com/repos/bittokazi/ktor-oauth2-multi-tenant-auth-server/releases/tags/$tag"
        return try {
            val dto = applicationHttpClient.get(url).body<GitHubChangeLogDto>()
            val lines =
                dto.body
                    ?.replace("\r\n", "\n")
                    ?.split('\n')
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()

            GithubChangeLogEntity(body = lines)
        } catch (e: Exception) {
            log.error("[GithubClient] error fetching changelog for tag=$tag", e)
            null
        }
    }
}
