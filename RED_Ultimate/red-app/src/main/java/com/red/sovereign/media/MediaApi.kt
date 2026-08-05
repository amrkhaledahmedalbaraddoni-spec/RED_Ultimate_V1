package com.red.sovereign.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.red.sovereign.auth.ApiResult
import com.red.sovereign.auth.AuthorizedApiClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

@Serializable data class MediaObject(val objectKey: String, val mimeType: String, val size: Long, val url: String)

class MediaApi(private val context: Context, private val client: AuthorizedApiClient) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun download(path: String, maximumBytes: Int = 25 * 1024 * 1024): ApiResult<ByteArray> {
        require(path.startsWith("/api/media/") && !path.contains("..")) { "Invalid authenticated media path" }
        return client.requestBytes(path, maximumBytes)
    }

    suspend fun upload(uri: Uri): ApiResult<MediaObject> {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: return ApiResult.Error(null, "UNKNOWN_MEDIA_TYPE")
        val name = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else "upload.bin"
        } ?: "upload.bin"
        val size = resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use {
            if (it.moveToFirst()) it.getLong(0) else -1L
        } ?: -1L
        val streamBody = object : RequestBody() {
            override fun contentType() = mime.toMediaType()
            override fun contentLength() = size
            override fun writeTo(sink: BufferedSink) {
                resolver.openInputStream(uri)?.use { input -> input.source().use { sink.writeAll(it) } }
                    ?: error("Unable to open selected media")
            }
        }
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", name, streamBody).build()
        return when (val result = client.requestBody("POST", "/api/media", body)) {
            is ApiResult.Success -> runCatching { ApiResult.Success(result.code, json.decodeFromString<MediaObject>(result.value)) }
                .getOrElse { ApiResult.Error(result.code, "INVALID_MEDIA_RESPONSE") }
            is ApiResult.Error -> result
        }
    }
}
