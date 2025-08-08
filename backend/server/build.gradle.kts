plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
    alias(libs.plugins.jib)
    alias(libs.plugins.serialization)
}

group = "com.earthrevealed.immaru"
version = "1.0.0-alpha"

application {
    mainClass.set("com.earthrevealed.immaru.ApplicationKt")
    applicationDefaultJvmArgs =
        listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

tasks.register<BuildInfoTask>("generate-build-info") {
    buildInfoOutput.set(file("${layout.buildDirectory.get()}/resources/main/build-info.properties"))
}

tasks.processResources {
    dependsOn("generate-build-info")
}

jib {
    to {
        image = "mminke/immaru-server"
        tags = setOf(System.getenv("GIT_COMMIT_SHA")?.take(7)?:"dev")
//        tags = setOf("$version", "$version.${extra["buildNumber"]}")
        auth {
            username = System.getenv("DOCKER_HUB_USERNAME")
            password = System.getenv("DOCKER_HUB_PASSWORD")
        }
    }
    container {
        volumes = listOf("/config", "/data")
        ports = listOf("8080")
        labels = mapOf(
            "maintainer" to "Morten Minke",
            "org.opencontainers.image.title" to "Immaru Server",
            "org.opencontainers.image.description" to "The backend server used to store and manage all assets.",
            "org.opencontainers.image.version" to "$version",
            "org.opencontainers.image.authors" to "Morten Minke",
            "org.opencontainers.image.url" to "https://github.com/mminke/immaru",
        )
    }
}

dependencies {
    implementation(projects.core)

    implementation(libs.microutils.kotlin.logging.jvm)
    runtimeOnly(libs.logback.classic)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.content.negotionation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.io.core)

    implementation(libs.bundles.flyway)
    runtimeOnly(libs.postgresql.jdbc)

    compileOnly(libs.r2dbc.spi)
    runtimeOnly(libs.postgresql.r2dbc)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}