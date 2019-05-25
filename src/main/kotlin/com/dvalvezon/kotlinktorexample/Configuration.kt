package com.dvalvezon.kotlinktorexample

import com.typesafe.config.ConfigFactory
import io.ktor.config.HoconApplicationConfig
import io.ktor.util.KtorExperimentalAPI


@KtorExperimentalAPI
object Configuration {

    private val config = HoconApplicationConfig(ConfigFactory.load())

    private val redisConfigs = config.config("redis")
    val redisHost: String = redisConfigs.property("host").getString()
    val redisPort: Int = redisConfigs.property("port").getString().toInt()

    private val rootConfigs = config.config("root")
    val rootKey: String = rootConfigs.property("key").getString()
}