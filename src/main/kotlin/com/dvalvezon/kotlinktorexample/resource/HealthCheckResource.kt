package com.dvalvezon.kotlinktorexample.resource

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

object HealthCheckResource {
    fun Routing.healthCheckResources() {
        get("/health_check") {
            call.respondText("OK")
        }
    }
}