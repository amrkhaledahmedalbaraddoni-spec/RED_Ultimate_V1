package com.red.sovereign.contacts

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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URLEncoder

@Serializable data class PublicRedProfile(val redId: String, val username: String, val displayName: String)

class DirectoryViewModel(application: Application) : AndroidViewModel(application) {
    private val client = AuthorizedApiClient(TokenStore(application))
    private val json = Json { ignoreUnknownKeys = true }
    val results = mutableStateListOf<PublicRedProfile>()
    var state: DirectoryState by mutableStateOf(DirectoryState.Idle); private set

    fun search(query: String) = viewModelScope.launch {
        val term = query.trim()
        if (term.length < 3) { state = DirectoryState.Error("اكتب 3 أحرف على الأقل"); return@launch }
        state = DirectoryState.Loading
        val encoded = URLEncoder.encode(term, "UTF-8")
        when (val response = client.request("GET", "/api/directory/search?query=$encoded")) {
            is ApiResult.Error -> state = DirectoryState.Error(response.message)
            is ApiResult.Success -> runCatching { json.decodeFromString<List<PublicRedProfile>>(response.value) }
                .onSuccess { results.clear(); results.addAll(it); state = DirectoryState.Ready }
                .onFailure { state = DirectoryState.Error("INVALID_DIRECTORY_RESPONSE") }
        }
    }

    fun clear() { results.clear(); state = DirectoryState.Idle }
}

sealed interface DirectoryState {
    data object Idle : DirectoryState
    data object Loading : DirectoryState
    data object Ready : DirectoryState
    data class Error(val message: String) : DirectoryState
}
