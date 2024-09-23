plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "com.earthrevealed.immaru"
version = "1.0.0"
application {
    mainClass.set("com.earthrevealed.immaru.ApplicationKt")
    applicationDefaultJvmArgs =
        listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.core)

    implementation(libs.microutils.kotlin.logging.jvm)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.content.negotionation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.flyway.core)
    runtimeOnly(libs.postgresql.jdbc)

    compileOnly(libs.r2dbc.spi)
    runtimeOnly(libs.postgresql.r2dbc)


    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}