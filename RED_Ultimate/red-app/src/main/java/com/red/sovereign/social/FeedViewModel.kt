package com.red.sovereign.social

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.red.sovereign.auth.ApiResult
import com.red.sovereign.auth.AuthorizedApiClient
import com.red.sovereign.auth.TokenStore
import kotlinx.coroutines.launch

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val api = FeedApi(AuthorizedApiClient(TokenStore(application)))
    val posts = mutableStateListOf<Post>()
    var state: FeedState by mutableStateOf(FeedState.Loading); private set
    var scope: String? = null; private set

    init { load(null) }

    fun load(newScope: String?) = viewModelScope.launch {
        scope = newScope; state = FeedState.Loading
        when (val result = api.load(newScope)) {
            is ApiResult.Success -> { posts.clear(); posts.addAll(result.value.posts); state = FeedState.Ready }
            is ApiResult.Error -> state = FeedState.Error(result.message)
        }
    }

    fun create(text: String, done: () -> Unit) = viewModelScope.launch {
        state = FeedState.Publishing
        when (val result = api.create(text)) {
            is ApiResult.Success -> { posts.add(0, result.value); state = FeedState.Ready; done() }
            is ApiResult.Error -> state = FeedState.Error(result.message)
        }
    }

    fun toggleLike(post: Post) = viewModelScope.launch {
        // The first feed version exposes aggregate counts only. Adding a LIKE is idempotent
        // per account on the server; viewer-specific unlike state comes with the social graph.
        when (val result = api.react(post.id, "LIKE", true)) {
            is ApiResult.Success -> posts.indexOfFirst { it.id == post.id }.takeIf { it >= 0 }?.let { posts[it] = result.value }
            is ApiResult.Error -> state = FeedState.Error(result.message)
        }
    }
}

sealed interface FeedState { data object Loading: FeedState; data object Ready: FeedState; data object Publishing: FeedState; data class Error(val message:String): FeedState }
