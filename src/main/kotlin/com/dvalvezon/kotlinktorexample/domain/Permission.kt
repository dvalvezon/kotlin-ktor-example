package com.dvalvezon.kotlinktorexample.domain

enum class Permission(val description: String) {
    ALL("All"),
    TEST_API("Test APIs"),
    USER_ADMIN_API("User Administration");

    companion object {

        val userAvailablePermissions = values().filter { ALL != it }

        fun getOrNull(name: String): Permission? {
            return try {
                valueOf(name)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}