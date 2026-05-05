package com.earthrevealed.ktor.extensions.common

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.contentType
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext

suspend inline fun RoutingContext.expectContentType(expectedContentType: ContentType, orElseBody: () -> Unit) {
    val contentType = call.request.contentType()
    if (!contentType.match(expectedContentType)) {
        call.respond(HttpStatusCode.UnsupportedMediaType, "Unsupported media type")
        return orElseBody()
    }
}