package com.dvalvezon.kotlinktorexample.resource

import com.dvalvezon.kotlinktorexample.domain.NewUserRequest
import com.dvalvezon.kotlinktorexample.domain.UpdateUserRequest
import com.dvalvezon.kotlinktorexample.framework.rootAuthorize
import com.dvalvezon.kotlinktorexample.resource.UserResource.UserResponse
import com.dvalvezon.kotlinktorexample.service.UserService
import com.dvalvezon.kotlinktorexample.utils.InvalidUser
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext

@KtorExperimentalAPI
object RootResource {

    fun Routing.rootResources() {
        route("/root/{rootKey}") {
            route("/user") {
                get {
                    rootAuthorize(getRootKey()) {
                        call.respond(HttpStatusCode.OK, UserResponse(UserService.bringAllUsers()))
                    }
                }
                get("/{userId}") {
                    rootAuthorize(getRootKey()) {
                        UserService.bringUser(call.parameters["userId"].orEmpty())
                                .let { call.respond(HttpStatusCode.OK, UserResponse(listOf(it))) }
                    }
                }
                post {
                    rootAuthorize(getRootKey()) {
                        call.receiveOrNull(NewUserRequest::class)
                                ?.toNewUser()
                                ?.validate { UserService.createUser(it) }
                                ?.let { UserResponse(listOf(it)) }
                                ?.let { call.respond(HttpStatusCode.Created, it) }
                                ?: throw InvalidUser(listOf(InvalidUser.InvalidUserCause.invalidNewUserPayload))
                    }
                }
                put("/{userId}") {
                    rootAuthorize(getRootKey()) {
                        UserService.bringUser(call.parameters["userId"].orEmpty())
                                .let { oldUser ->
                                    call.receiveOrNull(UpdateUserRequest::class)
                                            ?.toUpdatedNewUser(oldUser)
                                            ?.validate { UserService.changeUser(oldUser.id, it) }
                                            ?.let { UserResponse(listOf(it)) }
                                            ?.let { call.respond(HttpStatusCode.OK, it) }
                                            ?: throw InvalidUser(listOf(InvalidUser.InvalidUserCause.invalidUpdateUserPayload))
                                }
                    }
                }
            }
        }
    }

    private fun PipelineContext<Unit, ApplicationCall>.getRootKey(): String = call.parameters["rootKey"].orEmpty()
}

