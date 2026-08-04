package com.red.sovereign.auth

import com.red.sovereign.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable data class PstnCallRequest(val number: String)
@Serializable data class PstnCallResponse(val callId: String, val status: String, val number: String, val usedToday: Int, val dailyLimit: Int)

class PstnApi(private val tokens: TokenStore, private val auth: AuthApi = AuthApi()) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun dial(number: String): ApiResult<PstnCallResponse> = request(number, true)

    private suspend fun request(number: String, allowRefresh: Boolean): ApiResult<PstnCallResponse> = withContext(Dispatchers.IO) {
        val token = tokens.accessToken ?: return@withContext ApiResult.Error(401, "UNAUTHORIZED")
        val body = json.encodeToString(PstnCallRequest(number)).toRequestBody("application/json".toMediaType())
        runCatching {
            client.newCall(Request.Builder().url(BuildConfig.RED_SERVER_URL.trimEnd('/') + "/api/pstn/calls")
                .header("Authorization", "Bearer $token").post(body).build()).execute().use { response ->
                if (response.code == 401 && allowRefresh) {
                    val refresh = tokens.refreshToken ?: return@use ApiResult.Error(401, "UNAUTHORIZED")
                    when (val rotated = auth.refresh(refresh)) {
                        is ApiResult.Success -> { tokens.updateTokens(rotated.value); return@use request(number, false) }
                        is ApiResult.Error -> return@use rotated
                    }
                }
                val text = response.body.string()
                if (response.isSuccessful) ApiResult.Success(response.code, json.decodeFromString<PstnCallResponse>(text))
                else ApiResult.Error(response.code, runCatching { json.decodeFromString<ErrorResponse>(text).error }.getOrNull() ?: "PSTN_CALL_FAILED")
            }
        }.getOrElse { ApiResult.Error(null, it.message ?: "NETWORK_ERROR") }
    }
}
