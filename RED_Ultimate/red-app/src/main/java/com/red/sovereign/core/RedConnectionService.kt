package com.red.sovereign.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.red.sovereign.MainActivity
import com.red.sovereign.auth.ApiResult
import com.red.sovereign.auth.AuthApi
import com.red.sovereign.auth.DeviceKeyManager
import com.red.sovereign.auth.TokenStore
import com.red.sovereign.crypto.DecryptedMessage
import com.red.sovereign.crypto.DecryptedMessageBus
import com.red.sovereign.crypto.SignalSessionManager
import com.red.sovereign.proto.RedProtos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/** Local-first replacement for cloud push: an explicit foreground WebSocket connection. */
class RedConnectionService : Service() {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private var reconnectTask: ScheduledFuture<*>? = null
    private var attempts = 0
    @Volatile private var connected = false
    private val pendingSends = ConcurrentLinkedQueue<PendingText>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var tokenStore: TokenStore
    private lateinit var messageStore: MessageStore
    private lateinit var signal: SignalSessionManager
    private lateinit var keyManager: DeviceKeyManager
    private lateinit var socket: RedWebSocketClient

    override fun onCreate() {
        super.onCreate()
        createChannels()
        tokenStore = TokenStore(this)
        messageStore = MessageStore(this)
        signal = SignalSessionManager(this)
        keyManager = DeviceKeyManager(this)
        socket = RedWebSocketClient(tokenStore, ::onEnvelope, ::onState)
        scope.launch {
            if (signal.replenishPreKeys() is ApiResult.Error) {
                notifyConnection("تعذر تحديث مفاتيح الجلسات الآمنة — ستتم المحاولة عند إعادة الاتصال")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(CONNECTION_NOTIFICATION, connectionNotification("جارٍ الاتصال…"))
        if (intent?.action == ACTION_MARK_READ) {
            val messageId = intent.getStringExtra(EXTRA_MESSAGE_ID) ?: return START_STICKY
            socket.acknowledge(messageId, intent.getLongExtra(EXTRA_SEQUENCE, 0), "READ")
        } else if (intent?.action == ACTION_SEND_TEXT) {
            val target = intent.getStringExtra(EXTRA_TARGET) ?: return START_STICKY
            val conversation = intent.getStringExtra(EXTRA_CONVERSATION) ?: return START_STICKY
            val text = intent.getStringExtra(EXTRA_TEXT) ?: return START_STICKY
            pendingSends.add(PendingText(target, conversation, text))
            if (connected) drainSends() else socket.connect()
        } else socket.connect()
        return START_STICKY
    }

    private fun drainSends() {
        while (connected) {
            val pending = pendingSends.poll() ?: break
            sendEncryptedText(pending.target, pending.conversation, pending.text)
        }
    }

    private fun sendEncryptedText(target: String, conversation: String, text: String) {
        scope.launch {
            when (val encrypted = signal.encrypt(target, text.toByteArray(Charsets.UTF_8))) {
                is ApiResult.Error -> notifyConnection("فشل التشفير: ${encrypted.message}")
                is ApiResult.Success -> {
                    var firstId: String? = null
                    encrypted.value.forEach { envelope ->
                        val id = socket.sendEncrypted(target, conversation, "TEXT", keyManager.protocolDeviceId(), envelope)
                        if (firstId == null) firstId = id
                    }
                    firstId?.let { DecryptedMessageBus.publish(DecryptedMessage(it, conversation, tokenStore.redId.orEmpty(), text.toByteArray(), System.currentTimeMillis(), sequence = 0, outgoing = true)) }
                }
            }
        }
    }

    private fun onState(state: ConnectionState) {
        when (state) {
            ConnectionState.CONNECTED -> { connected = true; attempts = 0; reconnectTask?.cancel(false); notifyConnection("متصل بخادم RED المحلي"); drainSends() }
            ConnectionState.CONNECTING -> notifyConnection("جارٍ الاتصال بخادم RED المحلي")
            ConnectionState.DISCONNECTED -> { connected = false; scheduleReconnect() }
            ConnectionState.UNAUTHORIZED -> { connected = false; refreshAndReconnect() }
        }
    }

    private fun refreshAndReconnect() {
        val refresh = tokenStore.refreshToken ?: run { stopSelf(); return }
        scope.launch {
            when (val result = AuthApi().refresh(refresh)) {
                is ApiResult.Success -> { tokenStore.updateTokens(result.value); attempts = 0; socket.connect() }
                is ApiResult.Error -> { notifyConnection("انتهت الجلسة — افتح RED لتسجيل الدخول"); stopSelf() }
            }
        }
    }

    private fun scheduleReconnect() {
        reconnectTask?.cancel(false)
        val delay = minOf(60L, 1L shl minOf(attempts++, 6))
        notifyConnection("غير متصل — إعادة المحاولة خلال $delay ثانية")
        reconnectTask = scheduler.schedule({ socket.connect() }, delay, TimeUnit.SECONDS)
    }

    private fun onEnvelope(envelope: RedProtos.RedRED) {
        when (envelope.signalCase) {
            RedProtos.RedRED.SignalCase.MESSAGE -> {
                val message = envelope.message
                if (message.receiverId == tokenStore.redId && message.receiverDeviceId == keyManager.protocolDeviceId()) {
                    runCatching {
                        messageStore.save(message)
                        signal.decrypt(message.senderId, message.senderDeviceId, message.ciphertextType, message.payload.toByteArray())
                    }.onSuccess { plaintext ->
                        DecryptedMessageBus.publish(DecryptedMessage(message.id, message.conversationId, message.senderId, plaintext, message.timestamp, message.sequenceNumber))
                        socket.acknowledge(message.id, message.sequenceNumber, "DELIVERED")
                        notifyEncryptedMessage(message.senderId)
                    }
                } else if (message.senderId == tokenStore.redId) {
                    messageStore.save(message, "SENT")
                }
            }
            RedProtos.RedRED.SignalCase.ACK -> messageStore.updateStatus(envelope.ack.messageId, envelope.ack.status)
            RedProtos.RedRED.SignalCase.DELETE -> messageStore.delete(envelope.delete.messageId)
            else -> Unit
        }
    }

    private fun notifyEncryptedMessage(sender: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(sender.hashCode(), NotificationCompat.Builder(this, MESSAGE_CHANNEL)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("رسالة RED جديدة")
            .setContentText("رسالة مشفرة من $sender")
            .setContentIntent(openAppIntent())
            .setAutoCancel(true)
            .build())
    }

    private fun connectionNotification(text: String) = NotificationCompat.Builder(this, CONNECTION_CHANNEL)
        .setSmallIcon(android.R.drawable.stat_sys_upload_done)
        .setContentTitle("RED — الاتصال السيادي")
        .setContentText(text)
        .setContentIntent(openAppIntent())
        .setOngoing(true)
        .setSilent(true)
        .build()

    private fun notifyConnection(text: String) =
        getSystemService(NotificationManager::class.java).notify(CONNECTION_NOTIFICATION, connectionNotification(text))

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun createChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CONNECTION_CHANNEL, "اتصال RED المحلي", NotificationManager.IMPORTANCE_LOW))
        manager.createNotificationChannel(NotificationChannel(MESSAGE_CHANNEL, "رسائل RED", NotificationManager.IMPORTANCE_HIGH))
    }

    override fun onDestroy() {
        reconnectTask?.cancel(true); scheduler.shutdownNow(); scope.cancel(); socket.disconnect(); super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CONNECTION_CHANNEL = "red_connection"
        private const val MESSAGE_CHANNEL = "red_messages"
        private const val CONNECTION_NOTIFICATION = 7001
        private const val ACTION_SEND_TEXT = "com.red.sovereign.SEND_TEXT"
        private const val ACTION_MARK_READ = "com.red.sovereign.MARK_READ"
        private const val EXTRA_MESSAGE_ID = "message_id"
        private const val EXTRA_SEQUENCE = "sequence"
        private const val EXTRA_TARGET = "target"
        private const val EXTRA_CONVERSATION = "conversation"
        private const val EXTRA_TEXT = "text"

        fun start(context: Context) = context.startForegroundService(Intent(context, RedConnectionService::class.java))
        fun sendText(context: Context, targetRedId: String, conversationId: String, text: String) = context.startForegroundService(
            Intent(context, RedConnectionService::class.java).setAction(ACTION_SEND_TEXT)
                .putExtra(EXTRA_TARGET, targetRedId).putExtra(EXTRA_CONVERSATION, conversationId).putExtra(EXTRA_TEXT, text)
        )
        fun markRead(context: Context, messageId: String, sequence: Long) = context.startForegroundService(
            Intent(context, RedConnectionService::class.java).setAction(ACTION_MARK_READ)
                .putExtra(EXTRA_MESSAGE_ID, messageId).putExtra(EXTRA_SEQUENCE, sequence)
        )
        fun stop(context: Context) = context.stopService(Intent(context, RedConnectionService::class.java))
    }
}

private data class PendingText(val target: String, val conversation: String, val text: String)
