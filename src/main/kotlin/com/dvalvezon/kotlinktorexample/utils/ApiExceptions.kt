package com.dvalvezon.kotlinktorexample.utils

import io.ktor.http.HttpStatusCode

abstract class ErrorCause(val code: Long, val description: String)
private class BaseErrorCause(code: Long, description: String) : ErrorCause(code, description)
class ThrowableErrorCause(currentTime: Long) : ErrorCause(-1L, "Tell SysAdmin this number: $currentTime")

sealed class ApiException(val apiMessage: String, cause: Throwable?)
    : Throwable(apiMessage, cause) {

    abstract val causes: List<ErrorCause>
    abstract val httpStatusCode: HttpStatusCode

}

class UnexpectedException(additionalInfo: String, cause: Throwable? = null)
    : ApiException("Something unexpected happened in server-side. ($additionalInfo)", cause) {
    override val causes: List<ErrorCause> = listOf(BaseErrorCause(0L, apiMessage))
    override val httpStatusCode = HttpStatusCode.InternalServerError
}

class UsernameAlreadyExists(cause: Throwable? = null)
    : ApiException("Username already exists", cause) {
    override val causes: List<ErrorCause> = listOf(BaseErrorCause(1L, apiMessage))
    override val httpStatusCode = HttpStatusCode.Conflict
}

class UserNotFound(cause: Throwable? = null)
    : ApiException("User was not found", cause) {
    override val causes: List<ErrorCause> = listOf(BaseErrorCause(2L, apiMessage))
    override val httpStatusCode = HttpStatusCode.NotFound
}

class InvalidUser(invalidUserCause: List<InvalidUserCause>, cause: Throwable? = null)
    : ApiException(invalidUserCause.joinToString(separator = " - ", transform = { "${it.code}-${it.description}" }), cause) {

    class InvalidUserCause private constructor(code: Long, description: String) : ErrorCause(code, description) {
        companion object {
            val emptyUserName = InvalidUserCause(3L, "Empty userName")
            val emptyName = InvalidUserCause(4L, "Empty name")
            val emptyPassword = InvalidUserCause(5L, "Empty password")
            val invalidNewUserPayload = InvalidUserCause(6L, "Invalid new user payload")
            val invalidUpdateUserPayload = InvalidUserCause(7L, "Invalid update user payload")
        }
    }

    override val causes: List<ErrorCause> = invalidUserCause.toList()
    override val httpStatusCode = HttpStatusCode.BadRequest
}