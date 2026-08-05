package com.red.sovereign.stories

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.red.sovereign.auth.ApiResult
import com.red.sovereign.auth.AuthorizedApiClient
import com.red.sovereign.auth.TokenStore
import com.red.sovereign.media.MediaApi
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StoryViewModel(application: Application) : AndroidViewModel(application) {
    private val client = AuthorizedApiClient(TokenStore(application))
    private val media = MediaApi(application, client)
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    val stories = mutableStateListOf<Story>()
    var state: StoryState by mutableStateOf(StoryState.Idle); private set
    var viewer: StoryViewerState by mutableStateOf(StoryViewerState.Closed); private set

    init { load() }
    fun load() = viewModelScope.launch {
        state = StoryState.Loading
        when (val result = client.request("GET", "/api/stories")) {
            is ApiResult.Success -> runCatching { json.decodeFromString<List<Story>>(result.value) }
                .onSuccess { stories.clear(); stories.addAll(it); state = StoryState.Idle }
                .onFailure { state = StoryState.Error("INVALID_STORY_RESPONSE") }
            is ApiResult.Error -> state = StoryState.Error(result.message)
        }
    }

    fun upload(uri: Uri, caption: String? = null) = viewModelScope.launch {
        state = StoryState.Uploading
        when (val uploaded = media.upload(uri)) {
            is ApiResult.Error -> state = StoryState.Error(uploaded.message)
            is ApiResult.Success -> when (val created = client.request("POST", "/api/stories", json.encodeToString(CreateStoryRequest(uploaded.value.objectKey, caption)))) {
                is ApiResult.Success -> runCatching { json.decodeFromString<Story>(created.value) }
                    .onSuccess { stories.add(0, it); state = StoryState.Idle }
                    .onFailure { state = StoryState.Error("INVALID_STORY_RESPONSE") }
                is ApiResult.Error -> state = StoryState.Error(created.message)
            }
        }
    }

    fun open(story: Story) = viewModelScope.launch {
        viewed(story)
        if (!story.mediaType.startsWith("image/", ignoreCase = true)) {
            viewer = StoryViewerState.Unsupported(story, "عارض الفيديو الآمن قيد ربط Media3")
            return@launch
        }
        viewer = StoryViewerState.Loading(story)
        when (val result = media.download(story.mediaUrl)) {
            is ApiResult.Error -> viewer = StoryViewerState.Error(story, result.message)
            is ApiResult.Success -> {
                val bitmap = BitmapFactory.decodeByteArray(result.value, 0, result.value.size)
                viewer = if (bitmap == null) StoryViewerState.Error(story, "INVALID_IMAGE")
                else StoryViewerState.Image(story, bitmap.asImageBitmap())
            }
        }
    }

    fun closeViewer() { viewer = StoryViewerState.Closed }

    fun viewed(story: Story) = viewModelScope.launch {
        client.request("POST", "/api/stories/${story.id}/view")
    }
}

sealed interface StoryViewerState {
    data object Closed : StoryViewerState
    data class Loading(val story: Story) : StoryViewerState
    data class Image(val story: Story, val image: ImageBitmap) : StoryViewerState
    data class Unsupported(val story: Story, val message: String) : StoryViewerState
    data class Error(val story: Story, val message: String) : StoryViewerState
}

sealed interface StoryState { data object Idle: StoryState; data object Loading: StoryState; data object Uploading: StoryState; data class Error(val message:String): StoryState }
