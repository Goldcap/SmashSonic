package com.smashsonic.data.model

data class ServerConfig(
    val serverURL: String = "",
    val username: String = "",
    val password: String = "",
) {
    val isConfigured: Boolean
        get() = serverURL.isNotBlank() && username.isNotBlank() && password.isNotBlank()

    val baseURL: String
        get() {
            var url = serverURL.trim()
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }
            return url.trimEnd('/')
        }

    companion object {
        val Empty = ServerConfig()
    }
}
