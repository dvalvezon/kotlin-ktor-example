package com.dvalvezon.kotlinktorexample.resource

import com.dvalvezon.kotlinktorexample.domain.Permission
import com.dvalvezon.kotlinktorexample.utils.BaseResponse
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
object PermissionResource {

    fun Routing.permissionResources() {
        authenticate("jwt") {
            route("/permission") {
                get {
                    Permission.userAvailablePermissions
                            .map { PermissionObject(it.name, it.description) }
                            .let { call.respond(HttpStatusCode.OK, PermissionResponse(it)) }
                }
            }
        }
    }

    data class PermissionObject(val name: String, val description: String)

    class PermissionResponse(permissions: List<PermissionObject>)
        : BaseResponse<List<PermissionObject>>(responseData = permissions)
}