package com.dvalvezon.kotlinktorexample.service

import com.dvalvezon.kotlinktorexample.domain.Permission
import com.dvalvezon.kotlinktorexample.domain.UserPrincipal
import com.dvalvezon.kotlinktorexample.repository.UserRepository
import io.ktor.auth.UserPasswordCredential
import io.ktor.auth.jwt.JWTCredential
import io.ktor.util.KtorExperimentalAPI
import org.mindrot.jbcrypt.BCrypt

@KtorExperimentalAPI
object LoginService {

    fun getPrincipal(userName: String, password: String): UserPrincipal? =
            UserRepository.findUserByUserName(userName)?.let {
                if (BCrypt.checkpw(password, it.password) && it.enabled)
                    it.toPrincipal()
                else
                    null
            }

    fun validate(userPasswordCredential: UserPasswordCredential): UserPrincipal? =
            getPrincipal(userPasswordCredential.name, userPasswordCredential.password)

    fun validate2(it: JWTCredential): UserPrincipal? {
        val id = it.payload.getClaim("id")?.asString()
        val name = it.payload.getClaim("name")?.asString()
        val permissions = it.payload.getClaim("permissions")?.asArray(String::class.java)
                ?.map { Permission.getOrNull(it) }
                ?.let { it.filterNotNull() }
        return when {
            id == null -> null
            name == null -> null
            permissions == null -> null
            else -> UserPrincipal(id, name, permissions)
        }
    }

    fun validate(credential: JWTCredential): UserPrincipal? {
        return credential.payload.getClaim("id")
                ?.asString()
                ?.let { UserService.bringUser(it) }
                ?.let { user ->
                    val tokenName = credential.payload.getClaim("name")?.asString().orEmpty()
                    val tokenPermissions = credential.payload.getClaim("permissions")
                            ?.asArray(String::class.java)
                            ?.map { Permission.getOrNull(it) }
                            ?.let { it.filterNotNull() }
                            .orEmpty()
                    return when {
                        !user.name.contentEquals(tokenName) -> null
                        user.permissions.size != tokenPermissions.size -> null
                        !tokenPermissions.containsAll(user.permissions) -> null
                        else -> UserPrincipal(user.id, user.name, user.permissions)
                    }
                }
    }

}