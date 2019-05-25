package com.dvalvezon.kotlinktorexample.service

import com.dvalvezon.kotlinktorexample.domain.NewUser
import com.dvalvezon.kotlinktorexample.domain.User
import com.dvalvezon.kotlinktorexample.repository.UserRepository
import com.dvalvezon.kotlinktorexample.utils.UserNotFound
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
object UserService {

    fun createUser(newUser: NewUser): User = UserRepository.addUser(newUser)

    fun bringAllUsers(): List<User> = UserRepository.findAll()

    fun bringUser(userId: String): User = UserRepository.findUserById(userId).let {
        when (it) {
            null -> throw UserNotFound()
            else -> it
        }
    }

    fun changeUser(userId: String, changedUser: NewUser): User = UserRepository.updateUser(userId, changedUser)

}