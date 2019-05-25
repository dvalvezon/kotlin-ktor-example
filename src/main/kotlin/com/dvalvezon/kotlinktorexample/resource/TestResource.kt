package com.dvalvezon.kotlinktorexample.resource

import com.dvalvezon.kotlinktorexample.domain.Permission
import com.dvalvezon.kotlinktorexample.framework.authorize
import com.dvalvezon.kotlinktorexample.service.RequestService
import com.dvalvezon.kotlinktorexample.utils.BaseResponse
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
object TestResource {

    fun Routing.testResources() {
        authenticate("basic", "jwt") {
            get("/md5") {
                authorize(Permission.TEST_API) {
                    val url: String? = call.request.queryParameters["url"]
                    ContentResponse(RequestService.md5String(url))
                            .let { call.respond(HttpStatusCode.OK, it) }
                }
            }
        }
    }

    class ContentResponse(md5String: String)
        : BaseResponse<String>(responseData = md5String)

}