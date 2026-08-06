package com.red.sovereign.media

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.red.sovereign.auth.ApiResult
import com.red.sovereign.auth.AuthorizedApiClient
import com.red.sovereign.auth.TokenStore
import com.red.sovereign.core.RedConnectionService
import kotlinx.coroutines.launch

class AttachmentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EncryptedAttachmentRepository(application, AuthorizedApiClient(TokenStore(application)))
    var state: AttachmentState by mutableStateOf(AttachmentState.Idle); private set

    fun send(uri: Uri, targetRedId: String, conversationId: String) = viewModelScope.launch {
        if (state is AttachmentState.Working) return@launch
        state = AttachmentState.Working("جارٍ تشفير الملف ورفعه…")
        when (val result = repository.prepare(uri, targetRedId)) {
            is ApiResult.Error -> state = AttachmentState.Error(result.message)
            is ApiResult.Success -> {
                RedConnectionService.sendPayload(
                    getApplication(), targetRedId, conversationId, "FILE",
                    result.value.manifestJson.toByteArray(Charsets.UTF_8)
                )
                state = AttachmentState.Sent(result.value.name)
            }
        }
    }

    fun download(manifestJson: String) = viewModelScope.launch {
        if (state is AttachmentState.Working) return@launch
        state = AttachmentState.Working("جارٍ تنزيل الملف المشفر والتحقق منه…")
        state = when (val result = repository.downloadAndDecrypt(manifestJson)) {
            is ApiResult.Error -> AttachmentState.Error(result.message)
            is ApiResult.Success -> AttachmentState.Downloaded(result.value.absolutePath, result.value.name)
        }
    }

    fun clear() { state = AttachmentState.Idle }
}

sealed interface AttachmentState {
    data object Idle : AttachmentState
    data class Working(val message: String) : AttachmentState
    data class Sent(val name: String) : AttachmentState
    data class Downloaded(val path: String, val name: String) : AttachmentState
    data class Error(val message: String) : AttachmentState
}
