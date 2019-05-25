package com.dvalvezon.kotlinktorexample.framework

import com.dvalvezon.kotlinktorexample.Configuration
import com.dvalvezon.kotlinktorexample.domain.Permission
import com.dvalvezon.kotlinktorexample.domain.UserPrincipal
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authentication
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext

suspend fun PipelineContext<Unit, ApplicationCall>.authorize(vararg neededPermissions: Permission,
                                                             function: suspend () -> Unit) {
    require(neededPermissions.isNotEmpty()) { "At least one permission need to be provided" }
    val userPermissions = call.authentication.principal<UserPrincipal>()?.permissions ?: emptyList()
    if (userPermissions.contains(Permission.ALL) || neededPermissions.intersect(userPermissions).isNotEmpty()) {
        function.invoke()
    } else {
        call.respond(HttpStatusCode.Forbidden)
    }
}

@KtorExperimentalAPI
suspend fun PipelineContext<Unit, ApplicationCall>.rootAuthorize(key: String, function: suspend () -> Unit) {
    require(key.isNotEmpty()) { "Root key must not be empty" }
    if (Configuration.rootKey == key) {
        function.invoke()
    } else {
        call.respond(HttpStatusCode.Forbidden)
    }
}