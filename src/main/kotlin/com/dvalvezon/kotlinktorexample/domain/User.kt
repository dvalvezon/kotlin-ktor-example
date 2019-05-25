package com.dvalvezon.kotlinktorexample.domain

import com.dvalvezon.kotlinktorexample.framework.ValidateAble
import com.dvalvezon.kotlinktorexample.utils.InvalidUser
import com.dvalvezon.kotlinktorexample.utils.InvalidUser.InvalidUserCause

data class User(
        val id: String,
        val userName: String,
        val name: String,
        val password: String,
        val enabled: Boolean = true,
        val permissions: List<Permission>
) {
    fun toPrincipal(): UserPrincipal {
        return UserPrincipal(id, name, permissions)
    }
}

data class NewUser(
        val userName: String,
        val name: String,
        val password: String,
        val permissions: List<Permission>
) : ValidateAble<NewUser, InvalidUser.InvalidUserCause, NewUser> {
    override val myself = this
    override val validations: List<(NewUser) -> InvalidUserCause?> = listOf(
            { u -> if (u.name.isEmpty()) InvalidUserCause.emptyName else null },
            { u -> if (u.userName.isEmpty()) InvalidUserCause.emptyUserName else null },
            { u -> if (u.password.isEmpty()) InvalidUserCause.emptyPassword else null }
    )

    override fun validationError(errorCauses: List<InvalidUserCause>): Throwable = InvalidUser(errorCauses)

    override fun toValidatedType(): NewUser = this
}

data class NewUserRequest(
        val userName: String?,
        val name: String?,
        val password: String?,
        val permissions: List<String>?
) {
    fun toNewUser(): NewUser = NewUser(
            userName = userName.orEmpty(),
            name = name.orEmpty(),
            password = password.orEmpty(),
            permissions = permissions?.let { permissionString ->
                permissionString.mapNotNull { Permission.getOrNull(it) }
            } ?: emptyList()
    )
}

data class UpdateUserRequest(
        val name: String?,
        val password: String?,
        val permissions: List<String>?
) {
    fun toUpdatedNewUser(oldUser: User): NewUser = NewUser(
            userName = oldUser.userName,
            name = name ?: oldUser.name,
            password = password ?: oldUser.password,
            permissions = permissions?.let { permissionString ->
                permissionString.mapNotNull { Permission.getOrNull(it) }
            } ?: oldUser.permissions
    )
}