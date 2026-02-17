package com.smashsonic.data.remote

import java.security.MessageDigest

object SubsonicAuth {
    const val API_VERSION = "1.16.1"
    const val CLIENT_NAME = "SmashSonic"

    fun generateSalt(length: Int = 12): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length).map { chars.random() }.joinToString("")
    }

    fun generateToken(password: String, salt: String): String {
        val md5 = MessageDigest.getInstance("MD5")
        val digest = md5.digest((password + salt).toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun authParams(username: String, password: String): Map<String, String> {
        val salt = generateSalt()
        val token = generateToken(password, salt)
        return mapOf(
            "u" to username,
            "t" to token,
            "s" to salt,
            "v" to API_VERSION,
            "c" to CLIENT_NAME,
            "f" to "json",
        )
    }
}
