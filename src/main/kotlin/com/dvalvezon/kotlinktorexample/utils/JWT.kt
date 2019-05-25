package com.dvalvezon.kotlinktorexample.utils

import com.dvalvezon.kotlinktorexample.domain.UserPrincipal
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JWT {

    // 10 Hours
    private const val validityInMs = 36_000_00 * 10
    private const val secret = "aSecret"
    private const val issuer = "dvalvezon.com"
    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT
            .require(algorithm)
            .withIssuer(issuer)
            .build()

    fun createToken(user: UserPrincipal): String = JWT.create()
            .withSubject("Authentication")
            .withIssuer("dvalvezon.com")
            .withClaim("id", user.id)
            .withClaim("name", user.name)
            .withArrayClaim("permissions", user.permissions.map { it.name }.toTypedArray())
            .withExpiresAt(getExpiration())
            .sign(algorithm)

    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}