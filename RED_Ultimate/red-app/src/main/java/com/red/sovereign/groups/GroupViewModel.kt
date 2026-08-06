package com.red.sovereign.groups

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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GroupViewModel(application: Application) : AndroidViewModel(application) {
    private val client = AuthorizedApiClient(TokenStore(application))
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    val groups = mutableStateListOf<Group>()
    var state: GroupState by mutableStateOf(GroupState.Loading); private set
    init { load() }

    fun load() = viewModelScope.launch {
        state = GroupState.Loading
        when (val result = client.request("GET", "/api/groups")) {
            is ApiResult.Success -> runCatching { json.decodeFromString<List<Group>>(result.value) }
                .onSuccess { groups.clear(); groups.addAll(it); state = GroupState.Ready }
                .onFailure { state = GroupState.Error("INVALID_GROUP_RESPONSE") }
            is ApiResult.Error -> state = GroupState.Error(result.message)
        }
    }

    fun create(name: String, description: String?, done: () -> Unit) = viewModelScope.launch {
        state = GroupState.Saving
        when (val result = client.request("POST", "/api/groups", json.encodeToString(CreateGroupRequest(name, description)))) {
            is ApiResult.Success -> decodeAndStore(result.value, prepend = true, done)
            is ApiResult.Error -> state = GroupState.Error(result.message)
        }
    }

    fun addMember(group: Group, redId: String, done: () -> Unit) = viewModelScope.launch {
        state = GroupState.Saving
        when (val result = client.request("POST", "/api/groups/${group.id}/members", json.encodeToString(AddGroupMemberRequest(redId.trim().uppercase())))) {
            is ApiResult.Success -> decodeAndStore(result.value, done = done)
            is ApiResult.Error -> state = GroupState.Error(result.message)
        }
    }

    fun leave(group: Group, done: () -> Unit) = viewModelScope.launch {
        state = GroupState.Saving
        when (val result = client.request("DELETE", "/api/groups/${group.id}/membership")) {
            is ApiResult.Success -> { groups.removeAll { it.id == group.id }; state = GroupState.Ready; done() }
            is ApiResult.Error -> state = GroupState.Error(result.message)
        }
    }

    private fun decodeAndStore(value: String, prepend: Boolean = false, done: () -> Unit) {
        runCatching { json.decodeFromString<Group>(value) }
            .onSuccess { updated ->
                groups.indexOfFirst { it.id == updated.id }.takeIf { it >= 0 }?.let { groups[it] = updated }
                    ?: if (prepend) groups.add(0, updated) else groups.add(updated)
                state = GroupState.Ready
                done()
            }
            .onFailure { state = GroupState.Error("INVALID_GROUP_RESPONSE") }
    }
}

sealed interface GroupState { data object Loading:GroupState; data object Saving:GroupState; data object Ready:GroupState; data class Error(val message:String):GroupState }
