package com.red.sovereign.social

import com.red.sovereign.auth.ApiResult
import com.red.sovereign.auth.AuthorizedApiClient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder

class FeedApi(private val client: AuthorizedApiClient) {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    suspend fun load(scope: String?, cursor: String? = null): ApiResult<FeedResponse> {
        val query = buildList {
            scope?.let { add("scope=${URLEncoder.encode(it, "UTF-8")}") }
            cursor?.let { add("before=${URLEncoder.encode(it, "UTF-8")}") }
            add("limit=20")
        }.joinToString("&")
        return client.request("GET", "/api/feed?$query").decode { json.decodeFromString<FeedResponse>(it) }
    }

    suspend fun create(text: String, visibility: String = "LOCAL_YEMEN"): ApiResult<Post> =
        client.request("POST", "/api/feed/posts", json.encodeToString(CreatePostRequest(text, visibility))).decode { json.decodeFromString<Post>(it) }

    suspend fun react(postId: String, type: String, active: Boolean): ApiResult<Post> =
        client.request("POST", "/api/feed/posts/$postId/reactions", json.encodeToString(ReactionRequest(type, active))).decode { json.decodeFromString<Post>(it) }

    private inline fun <T> ApiResult<String>.decode(block: (String) -> T): ApiResult<T> = when (this) {
        is ApiResult.Success -> runCatching { ApiResult.Success(code, block(value)) }.getOrElse { ApiResult.Error(code, "INVALID_SERVER_RESPONSE") }
        is ApiResult.Error -> this
    }
}
