package com.dvalvezon.kotlinktorexample.resource

import com.dvalvezon.kotlinktorexample.service.LoginService
import com.dvalvezon.kotlinktorexample.utils.JWT
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.util.KtorExperimentalAPI


@KtorExperimentalAPI
object LoginResource {

    fun Routing.loginResources() {
        post("/login") {
            call.receiveOrNull<UserAndPassword>()
                    ?.let { up ->
                        when {
                            up.user == null || up.password == null ->
                                call.respond(HttpStatusCode.BadRequest, "UserPrincipal/Password must be provided.")
                            else ->
                                LoginService.getPrincipal(up.user, up.password)
                                        ?.let { JWT.createToken(it) }
                                        ?.let { call.respond(it) }
                                        ?: call.respond(HttpStatusCode.NotFound, "Invalid credentials.")
                        }
                    }
        }
    }

    private data class UserAndPassword(val user: String?, val password: String?)

}