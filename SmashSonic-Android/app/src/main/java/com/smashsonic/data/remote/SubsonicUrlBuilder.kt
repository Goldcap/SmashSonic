package com.smashsonic.data.remote

import com.smashsonic.credential.CredentialManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubsonicUrlBuilder @Inject constructor(
    private val credentialManager: CredentialManager,
) {
    fun buildUrl(endpoint: String, additionalParams: Map<String, String> = emptyMap()): String? {
        val config = credentialManager.loadServerConfig() ?: return null
        if (!config.isConfigured) return null

        val params = SubsonicAuth.authParams(config.username, config.password).toMutableMap()
        params.putAll(additionalParams)

        val queryString = params.entries.joinToString("&") { (k, v) ->
            "${java.net.URLEncoder.encode(k, "UTF-8")}=${java.net.URLEncoder.encode(v, "UTF-8")}"
        }
        return "${config.baseURL}/rest/$endpoint?$queryString"
    }

    fun streamUrl(songId: String, maxBitRate: Int? = null): String? {
        val params = mutableMapOf("id" to songId)
        maxBitRate?.let { params["maxBitRate"] = it.toString() }
        return buildUrl("stream", params)
    }

    fun coverArtUrl(coverArtId: String, size: Int = 300): String? {
        return buildUrl("getCoverArt", mapOf("id" to coverArtId, "size" to size.toString()))
    }

    fun downloadUrl(songId: String): String? {
        return buildUrl("download", mapOf("id" to songId))
    }
}
