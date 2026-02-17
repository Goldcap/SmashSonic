package com.smashsonic.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smashsonic.credential.CredentialManager
import com.smashsonic.data.model.ServerConfig
import com.smashsonic.data.remote.SubsonicApi
import com.smashsonic.data.remote.SubsonicAuth
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ServerSetupViewModel @Inject constructor(
    private val credentialManager: CredentialManager,
    private val moshi: Moshi,
) : ViewModel() {

    private val _serverURL = MutableStateFlow("")
    val serverURL: StateFlow<String> = _serverURL.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()

    private val _testResult = MutableStateFlow<TestResult?>(null)
    val testResult: StateFlow<TestResult?> = _testResult.asStateFlow()

    private val _isConfigured = MutableStateFlow(false)
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()

    init {
        loadCurrentConfig()
    }

    fun loadCurrentConfig() {
        val config = credentialManager.loadServerConfig()
        if (config != null && config.isConfigured) {
            _serverURL.value = config.serverURL
            _username.value = config.username
            _password.value = config.password
            _isConfigured.value = true
        }
    }

    fun updateServerURL(value: String) { _serverURL.value = value }
    fun updateUsername(value: String) { _username.value = value }
    fun updatePassword(value: String) { _password.value = value }

    val isFormValid: Boolean
        get() = _serverURL.value.isNotBlank() && _username.value.isNotBlank() && _password.value.isNotEmpty()

    fun testConnection() {
        if (!isFormValid) return
        _isTesting.value = true
        _testResult.value = null

        viewModelScope.launch {
            try {
                val config = buildConfig()
                val success = testPing(config)
                _testResult.value = if (success) TestResult.Success else TestResult.Failure("Could not connect to server. Check your URL and credentials.")
            } catch (e: Exception) {
                _testResult.value = TestResult.Failure(e.message ?: "Connection failed")
            } finally {
                _isTesting.value = false
            }
        }
    }

    fun saveConfiguration() {
        val config = buildConfig()
        credentialManager.saveServerConfig(config)
        _isConfigured.value = true
    }

    fun signOut() {
        credentialManager.deleteServerConfig()
        _serverURL.value = ""
        _username.value = ""
        _password.value = ""
        _testResult.value = null
        _isConfigured.value = false
    }

    private fun buildConfig() = ServerConfig(
        serverURL = _serverURL.value.trim(),
        username = _username.value.trim(),
        password = _password.value,
    )

    private suspend fun testPing(config: ServerConfig): Boolean {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val params = SubsonicAuth.authParams(config.username, config.password)
                val urlBuilder = original.url.newBuilder()
                params.forEach { (key, value) -> urlBuilder.addQueryParameter(key, value) }
                chain.proceed(original.newBuilder().url(urlBuilder.build()).build())
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("${config.baseURL}/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val api = retrofit.create(SubsonicApi::class.java)
        val response = api.ping()
        return response.subsonicResponse.status == "ok"
    }

    sealed class TestResult {
        data object Success : TestResult()
        data class Failure(val message: String) : TestResult()
    }
}
