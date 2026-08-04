package com.red.sovereign.core

import com.google.protobuf.ByteString
import com.red.sovereign.BuildConfig
import com.red.sovereign.auth.TokenStore
import com.red.sovereign.proto.RedProtos
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import java.util.concurrent.TimeUnit

class RedWebSocketClient(
    private val tokens: TokenStore,
    private val onEnvelope: (RedProtos.RedRED) -> Unit,
    private val onState: (ConnectionState) -> Unit = {}
) {
    private val client = OkHttpClient.Builder().pingInterval(25, TimeUnit.SECONDS).build()
    private var socket: WebSocket? = null

    fun connect() {
        val token = tokens.accessToken ?: return onState(ConnectionState.UNAUTHORIZED)
        val wsBase = BuildConfig.RED_SERVER_URL.replaceFirst("http://", "ws://").replaceFirst("https://", "wss://")
        val request = Request.Builder().url(wsBase.trimEnd('/') + "/ws/master")
            .header("Authorization", "Bearer $token").build()
        onState(ConnectionState.CONNECTING)
        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) = onState(ConnectionState.CONNECTED)
            override fun onMessage(webSocket: WebSocket, bytes: okio.ByteString) {
                runCatching { RedProtos.RedRED.parseFrom(bytes.toByteArray()) }.onSuccess(onEnvelope)
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) = onState(ConnectionState.DISCONNECTED)
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) =
                onState(if (response?.code == 401) ConnectionState.UNAUTHORIZED else ConnectionState.DISCONNECTED)
        })
    }

    fun sendText(receiverRedId: String, conversationId: String, encryptedPayload: ByteArray): String {
        val sender = requireNotNull(tokens.redId) { "RED ID is unavailable" }
        val id = UuidV7.next()
        val chat = RedProtos.ChatMessage.newBuilder()
            .setId(id).setConversationId(conversationId).setSenderId(sender).setReceiverId(receiverRedId)
            .setPayload(ByteString.copyFrom(encryptedPayload)).setTimestamp(System.currentTimeMillis()).setType("TEXT").build()
        val envelope = RedProtos.RedRED.newBuilder().setMessage(chat).build()
        check(socket?.send(envelope.toByteArray().toByteString()) == true) { "RED WebSocket is not connected" }
        return id
    }

    fun acknowledge(messageId: String, sequence: Long, status: String) {
        require(status == "DELIVERED" || status == "READ")
        val envelope = RedProtos.RedRED.newBuilder().setAck(
            RedProtos.MessageAck.newBuilder().setMessageId(messageId).setSequenceNumber(sequence).setStatus(status)
        ).build()
        check(socket?.send(envelope.toByteArray().toByteString()) == true) { "RED WebSocket is not connected" }
    }

    fun typing(conversationId: String, targetRedId: String, active: Boolean) {
        val sender = requireNotNull(tokens.redId)
        val envelope = RedProtos.RedRED.newBuilder().setTyping(
            RedProtos.TypingRED.newBuilder().setConversationId(conversationId).setUserId(sender)
                .setTargetUserId(targetRedId).setIsTyping(active)
        ).build()
        socket?.send(envelope.toByteArray().toByteString())
    }

    fun disconnect() { socket?.close(1000, "client logout"); socket = null }
}

enum class ConnectionState { CONNECTING, CONNECTED, DISCONNECTED, UNAUTHORIZED }
