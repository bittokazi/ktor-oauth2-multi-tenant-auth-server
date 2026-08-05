plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    // linter
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

group = "com.bittokazi.watch.navigator"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

val testKtorOauthAuthLibrary: Boolean = false

dependencies {
    implementation(libs.ktor.server.rate.limiting)
    implementation(libs.ktor.server.task.scheduling.core)
    implementation(libs.ktor.server.task.scheduling.redis)
    implementation(libs.ktor.server.task.scheduling.mongodb)
    implementation(libs.ktor.server.task.scheduling.jdbc)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.websockets)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.postgresql)
    implementation(libs.h2)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.ktor.server.mustache)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.sessions)
    implementation(libs.ktor.server.csrf)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.partial.content)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.forwarded.header)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.hikari)
    implementation(libs.flyway.core)
    implementation(libs.bcrypt)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.nimbus.jose.jwt)
    implementation(libs.ktor.server.di)
    implementation(libs.ktor.oauth.authorization.server)
    implementation("org.eclipse.angus:jakarta.mail:2.0.4")
    implementation("com.warrenstrange:googleauth:1.4.0")
    implementation("io.ktor:ktor-server-swagger:3.4.0")
    implementation("io.ktor:ktor-server-routing-openapi:3.4.0")

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
