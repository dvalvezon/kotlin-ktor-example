package com.dvalvezon.kotlinktorexample.utils

abstract class BaseResponse<T>(
        val message: String = "Success",
        val responseData: T?,
        val errors: List<ErrorCause> = listOf()
)

class FailureResponse(errors: List<ErrorCause>)
    : BaseResponse<Nothing>(message = "Failure", responseData = null, errors = errors)