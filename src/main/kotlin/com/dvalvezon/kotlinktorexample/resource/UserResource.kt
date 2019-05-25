package com.dvalvezon.kotlinktorexample.resource

import com.dvalvezon.kotlinktorexample.domain.*
import com.dvalvezon.kotlinktorexample.framework.authorize
import com.dvalvezon.kotlinktorexample.service.UserService
import com.dvalvezon.kotlinktorexample.utils.BaseResponse
import com.dvalvezon.kotlinktorexample.utils.InvalidUser
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
object UserResource {

    fun Routing.userResources() {
        authenticate("jwt") {
            route("/user") {
                get {
                    authorize(Permission.USER_ADMIN_API) {
                        call.respond(HttpStatusCode.OK, UserResponse(UserService.bringAllUsers()))
                    }
                }
                get("/{userId}") {
                    authorize(Permission.USER_ADMIN_API) {
                        UserService.bringUser(call.parameters["userId"].orEmpty())
                                .let { call.respond(HttpStatusCode.OK, UserResponse(listOf(it))) }
                    }
                }
                post {
                    authorize(Permission.USER_ADMIN_API) {
                        call.receiveOrNull(NewUserRequest::class)
                                ?.toNewUser()
                                ?.let { removeAllPermission(it) }
                                ?.validate { UserService.createUser(it) }
                                ?.let { UserResponse(listOf(it)) }
                                ?.let { call.respond(HttpStatusCode.Created, it) }
                                ?: throw InvalidUser(listOf(InvalidUser.InvalidUserCause.invalidNewUserPayload))
                    }
                }
                put("/{userId}") {
                    authorize(Permission.USER_ADMIN_API) {
                        UserService.bringUser(call.parameters["userId"].orEmpty())
                                .let { oldUser ->
                                    call.receiveOrNull(UpdateUserRequest::class)
                                            ?.toUpdatedNewUser(oldUser)
                                            ?.let { removeAllPermission(it) }
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

    private fun removeAllPermission(user: NewUser): NewUser =
            user.copy(permissions = user.permissions.filter { Permission.ALL != it })


    class UserResponse(user: List<User>)
        : BaseResponse<List<User>>(responseData = user.map { it.copy(password = "*****") })
}