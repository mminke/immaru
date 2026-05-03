package com.earthrevealed.immaru

import com.earthrevealed.immaru.routes.api.api
import com.earthrevealed.ktor.extensions.buildinfo.buildInfo
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Options
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.resources.Resources
import io.ktor.server.routing.routing
import org.flywaydb.core.Flyway

fun main() {

    embeddedServer(
        Netty,
        port = Configuration.immaru.server.port,
        host = Configuration.immaru.server.host,
        module = Application::module
    )
        .start(wait = true)
}

fun Application.module() {
    install(CallLogging)
    install(CORS) {
        this.allowMethod(Get)
        this.allowMethod(Options)
        this.allowMethod(Post)
        this.allowMethod(Put)
        this.allowMethod(Delete)
        anyHost()
        this.allowHeader("Content-Type")
    }
    install(Resources)
    configureDI()
    configureDatabaseSchema()
    configureContentNegotiation()
    configureRouting()
}

fun Application.configureDI() {
//    install(Koin) {
//        slf4jLogger()
//        modules(applicationModule)
//    }
}

fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json()
    }
}

fun configureDatabaseSchema() {
    val flyway = Flyway.configure()
        .dataSource(
            Configuration.immaru.database.flyway.jdbc.url,
            Configuration.immaru.database.flyway.jdbc.username,
            Configuration.immaru.database.flyway.jdbc.password
        )
        .schemas("immaru")
        .locations("classpath:db/migration")
        .load()

    flyway.migrate()
}

fun Application.configureRouting() {
    routing {
        api()
        buildInfo()
    }
}
