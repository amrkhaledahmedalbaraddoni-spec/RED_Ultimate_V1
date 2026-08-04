package com.red.sovereign.crypto

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class DecryptedMessage(val id: String, val conversationId: String, val senderRedId: String, val plaintext: ByteArray, val timestamp: Long, val sequence: Long, val outgoing: Boolean = false)

object DecryptedMessageBus {
    private val mutable = MutableSharedFlow<DecryptedMessage>(extraBufferCapacity = 128)
    val messages = mutable.asSharedFlow()
    fun publish(message: DecryptedMessage) { mutable.tryEmit(message) }
}
