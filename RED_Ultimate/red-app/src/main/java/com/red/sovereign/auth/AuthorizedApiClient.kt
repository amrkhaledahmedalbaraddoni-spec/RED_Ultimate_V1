package com.red.sovereign.auth

import com.red.sovereign.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AuthorizedApiClient(
    private val tokens: TokenStore,
    private val auth: AuthApi = AuthApi(),
    private val client: OkHttpClient = OkHttpClient()
) {
    suspend fun request(method: String, path: String, jsonBody: String? = null): ApiResult<String> {
        var result = execute(method, path, jsonBody)
        if (result is ApiResult.Error && result.code == 401) {
            val refresh = tokens.refreshToken ?: return result
            when (val rotated = auth.refresh(refresh)) {
                is ApiResult.Success -> { tokens.updateTokens(rotated.value); result = execute(method, path, jsonBody) }
                is ApiResult.Error -> return rotated
            }
        }
        return result
    }

    private suspend fun execute(method: String, path: String, jsonBody: String?): ApiResult<String> = withContext(Dispatchers.IO) {
        val token = tokens.accessToken ?: return@withContext ApiResult.Error(401, "UNAUTHORIZED")
        runCatching {
            val builder = Request.Builder().url(BuildConfig.RED_SERVER_URL.trimEnd('/') + path)
                .header("Authorization", "Bearer $token").header("Accept", "application/json")
            val body = jsonBody?.toRequestBody(JSON)
            when (method) {
                "GET" -> builder.get()
                "POST" -> builder.post(body ?: EMPTY)
                "PUT" -> builder.put(body ?: EMPTY)
                "DELETE" -> builder.delete(body)
                else -> error("Unsupported HTTP method")
            }
            client.newCall(builder.build()).execute().use { response ->
                val text = response.body.string()
                if (response.isSuccessful) ApiResult.Success(response.code, text)
                else ApiResult.Error(response.code, text.ifBlank { "HTTP_${response.code}" })
            }
        }.getOrElse { ApiResult.Error(null, it.message ?: "NETWORK_ERROR") }
    }

    private companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
        val EMPTY = ByteArray(0).toRequestBody(null)
    }
}
