package com.dvalvezon.kotlinktorexample.repository

import com.dvalvezon.kotlinktorexample.Configuration
import com.dvalvezon.kotlinktorexample.domain.NewUser
import com.dvalvezon.kotlinktorexample.domain.User
import com.dvalvezon.kotlinktorexample.utils.UnexpectedException
import com.dvalvezon.kotlinktorexample.utils.UserNotFound
import com.dvalvezon.kotlinktorexample.utils.UsernameAlreadyExists
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.util.KtorExperimentalAPI
import org.mindrot.jbcrypt.BCrypt
import redis.clients.jedis.Jedis
import java.util.*

@KtorExperimentalAPI
object UserRepository {

    private val kotlinMapper = jacksonObjectMapper()

    private const val userIdPrefix = "user"
    private const val usersKey = "users"
    private const val userNamesKey = "userNames"

    fun addUser(newUser: NewUser): User {
        val client = getClient()

        tailrec fun getUniqueUserUUID(): String {
            val uuid = UUID.randomUUID().toString()
            return if (client.hexists(usersKey, "$userIdPrefix-$uuid")) getUniqueUserUUID() else uuid
        }

        return when (client.hget(userNamesKey, newUser.userName)) {
            null -> {
                val user = User(
                        id = getUniqueUserUUID(),
                        userName = newUser.userName,
                        name = newUser.name,
                        password = BCrypt.hashpw(newUser.password, BCrypt.gensalt(6)),
                        permissions = newUser.permissions
                )
                if (client.hset(userNamesKey, user.userName, user.id) == 0L)
                    throw UnexpectedException("User Creation - userName/id")
                if (client.hset(usersKey, "$userIdPrefix-${user.id}", kotlinMapper.writeValueAsString(user)) == 0L)
                    throw UnexpectedException("User Creation - id/value")

                user
            }
            else -> {
                throw UsernameAlreadyExists()
            }
        }
    }

    fun findUserById(uuid: String): User? {
        val client = getClient()
        val userJsonString: String? = client.hget(usersKey, "$userIdPrefix-$uuid")
        return when (userJsonString) {
            null -> null
            else -> kotlinMapper.readValue(userJsonString, User::class.java)
        }
    }

    fun findUserByUserName(userName: String): User? {
        val client = getClient()
        val userId: String? = client.hget(userNamesKey, userName)
        return when (userId) {
            null -> null
            else -> findUserById(userId)
        }
    }

    fun findAll(): List<User> = getClient().hgetAll(usersKey).map { kotlinMapper.readValue(it.value, User::class.java) }

    private fun getClient(): Jedis {
        return Jedis(Configuration.redisHost, Configuration.redisPort)
    }

    fun updateUser(userId: String, changedUser: NewUser): User {
        val client = getClient()
        if (client.hexists(usersKey, "$userIdPrefix-$userId")) {
            val user = User(
                    id = userId,
                    userName = changedUser.userName,
                    name = changedUser.name,
                    password = BCrypt.hashpw(changedUser.password, BCrypt.gensalt(6)),
                    permissions = changedUser.permissions
            )
            client.hset(usersKey, "$userIdPrefix-$userId", kotlinMapper.writeValueAsString(user))
            return user
        } else {
            throw UserNotFound()
        }

    }
}