package com.dvalvezon.kotlinktorexample.domain

import io.ktor.auth.Principal

data class UserPrincipal(
        val id: String,
        val name: String,
        val permissions: List<Permission>
) : Principal