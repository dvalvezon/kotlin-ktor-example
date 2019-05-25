package com.dvalvezon.kotlinktorexample

import com.dvalvezon.kotlinktorexample.resource.TestResource.testResources
import com.dvalvezon.kotlinktorexample.resource.HealthCheckResource.healthCheckResources
import com.dvalvezon.kotlinktorexample.resource.LoginResource.loginResources
import com.dvalvezon.kotlinktorexample.resource.PermissionResource.permissionResources
import com.dvalvezon.kotlinktorexample.resource.RootResource.rootResources
import com.dvalvezon.kotlinktorexample.resource.UserResource.userResources
import com.dvalvezon.kotlinktorexample.service.LoginService
import com.dvalvezon.kotlinktorexample.utils.*
import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.basic
import io.ktor.auth.jwt.jwt
import io.ktor.features.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(StatusPages) {
        exception<UnexpectedException> { exception ->
            log.error("Critical - Unexpected exception - message='${exception.apiMessage}'")
            call.respond(exception.httpStatusCode, FailureResponse(exception.causes))
        }
        exception<ApiException> { exception ->
            call.respond(exception.httpStatusCode, FailureResponse(exception.causes))
        }
        exception<Throwable> { cause ->
            val time = System.currentTimeMillis()
            log.error("Critical - Unmapped exception - time='$time'", cause)
            call.respond(HttpStatusCode.InternalServerError, FailureResponse(listOf(ThrowableErrorCause(time))))
        }
    }
    install(CORS) {
        anyHost()
        allowCredentials = true

        header(HttpHeaders.ContentType)
        header(HttpHeaders.Origin)
        header(HttpHeaders.Referrer)
        header(HttpHeaders.UserAgent)
        header(HttpHeaders.AccessControlRequestHeaders)
        header(HttpHeaders.AccessControlRequestMethod)
        header(HttpHeaders.Authorization)

        method(HttpMethod.Head)
        method(HttpMethod.Options)
        method(HttpMethod.Patch)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
    }
    install(ContentNegotiation) {
        jackson {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
    install(Authentication) {
        basic("basic") {
            realm = "defaultRealm"
            validate {
                LoginService.validate(it)
            }
        }
        jwt("jwt") {
            verifier(JWT.verifier)
            realm = "defaultRealm"
            validate {
                LoginService.validate(it)
            }
        }
    }
    install(Routing) {
        routing {
            healthCheckResources()
            rootResources()
            loginResources()
            permissionResources()
            userResources()
            testResources()
        }
    }
}