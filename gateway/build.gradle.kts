val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"

    // for publishing
    id("signing")
    id("maven-publish")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"

    // for code coverage
    jacoco

    // linter
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

group = "com.bittokazi.ktor.auth.server"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation("io.ktor:ktor-server-di")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-serialization-jackson")
    implementation("io.ktor:ktor-serialization-gson")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-partial-content")
    implementation("io.ktor:ktor-server-forwarded-header")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-sessions")
    implementation("ch.qos.logback:logback-classic:1.5.13")

    implementation("com.bittokazi.sonartype:ktor-oauth-authorization-server:1.3.1")
    implementation("com.bittokazi.sonartype:ktor-gateway:1.0.3")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

tasks.test {
    useJUnitPlatform {
        includeEngines("junit-jupiter", "junit-vintage")
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

ktlint {
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Ktor Oauth Authorization Server")
                description.set("Oauth Authorization Server Library for Ktor Framework")
                url.set("https://github.com/bittokazi/ktor-oauth-authorization-server")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("bittokazi")
                        name.set("Bitto Kazi")
                        email.set("bitto.kazi@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/bittokazi/ktor-oauth-authorization-server.git")
                    developerConnection.set("scm:git:ssh://github.com/bittokazi/ktor-oauth-authorization-server.git")
                    url.set("github.com/bittokazi/ktor-oauth-authorization-server")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("CENTRAL_PUBLISHER_USERNAME"))
            password.set(System.getenv("CENTRAL_PUBLISHER_PASSWORD"))
        }
    }
}

val signingKey: String? = System.getenv("SIGNING_KEY")

signing {
    useInMemoryPgpKeys(signingKey, "")
    sign(publishing.publications["mavenJava"])
}
