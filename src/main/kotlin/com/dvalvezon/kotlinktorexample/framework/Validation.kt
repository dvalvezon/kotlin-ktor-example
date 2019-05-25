package com.dvalvezon.kotlinktorexample.framework

interface ValidateAble<T : ValidateAble<T, R, S>, R : Any, S : Any> {

    val validations: List<(T) -> R?>
    val myself: T

    fun validationError(errorCauses: List<R>): Throwable

    fun toValidatedType(): S

    suspend fun <X> validate(function: suspend (S) -> X): X {
        validations.mapNotNull { it.invoke(myself) }.let {
            return if (it.isEmpty())
                function.invoke(toValidatedType())
            else
                throw validationError(it)
        }
    }
}