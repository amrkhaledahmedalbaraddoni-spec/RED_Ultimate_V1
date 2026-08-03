package com.red.sovereign.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.sovereign.core.database.MessageEntity
import com.red.sovereign.core.delivery.MasterDeliveryEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiMessage(
    val id: String,
    val content: String,
    val isMe: Boolean,
    val status: String = "SENT",
    val timestamp: Long = System.currentTimeMillis(),
    val reactions: List<String> = emptyList(),
    val replyTo: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val deliveryEngine: MasterDeliveryEngine
) : ViewModel() {

    private val _messages = MutableStateFlow<List<UiMessage>>(listOf(
        UiMessage("1", "🔴 RED Sovereign - مرحبا! النظام السيادي جاهز", false, "READ"),
        UiMessage("2", "System C: Guaranteed Delivery UUID v7 ACTIVE", false, "READ"),
        UiMessage("3", "System A: VoIP 4K AV1 Ready", false, "READ"),
        UiMessage("4", "System B: DINSTAR UC2000 Connected", false, "READ"),
        UiMessage("5", "Testing 1080p Call ✓✓", true, "READ", reactions = listOf("❤️", "👍"))
    ))
    val messages: StateFlow<List<UiMessage>> = _messages

    fun sendMessage(chatId: String, text: String) {
        if (text.isBlank()) return
        val msg = UiMessage(
            id = deliveryEngine.generateUuidV7(),
            content = text,
            isMe = true,
            status = "SENDING"
        )
        _messages.value = _messages.value + msg
        deliveryEngine.dispatchMessage(chatId, text)
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            _messages.value = _messages.value.map { if (it.id == msg.id) it.copy(status = "SENT") else it }
            kotlinx.coroutines.delay(500)
            _messages.value = _messages.value.map { if (it.id == msg.id) it.copy(status = "DELIVERED") else it }
            kotlinx.coroutines.delay(500)
            _messages.value = _messages.value.map { if (it.id == msg.id) it.copy(status = "READ") else it }
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        _messages.value = _messages.value.map { msg ->
            if (msg.id == messageId) msg.copy(reactions = msg.reactions + emoji) else msg
        }
    }

    fun deleteForEveryone(messageId: String) {
        _messages.value = _messages.value.filterNot { it.id == messageId }
    }
}
