package com.dvalvezon.kotlinktorexample.service

import io.ktor.util.KtorExperimentalAPI
import kotlinx.io.core.toByteArray
import java.security.MessageDigest

@KtorExperimentalAPI
object RequestService {

    fun md5String(url: String?): String = url?.md5() ?: ""

    private fun String.md5(): String {
        val digested = MessageDigest.getInstance("MD5").digest(toByteArray())
        return digested.joinToString("") {
            String.format("%02x", it)
        }
    }
}