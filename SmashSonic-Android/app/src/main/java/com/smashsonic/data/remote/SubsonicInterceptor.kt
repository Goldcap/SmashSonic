package com.smashsonic.data.remote

import com.smashsonic.credential.CredentialManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubsonicInterceptor @Inject constructor(
    private val credentialManager: CredentialManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val config = credentialManager.loadServerConfig() ?: return chain.proceed(original)

        val params = SubsonicAuth.authParams(config.username, config.password)
        val urlBuilder = original.url.newBuilder()
        params.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }

        val newRequest = original.newBuilder()
            .url(urlBuilder.build())
            .build()
        return chain.proceed(newRequest)
    }
}
