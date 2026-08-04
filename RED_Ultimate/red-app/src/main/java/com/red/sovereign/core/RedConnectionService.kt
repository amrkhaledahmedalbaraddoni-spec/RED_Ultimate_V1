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
import com.red.sovereign.auth.TokenStore
import com.red.sovereign.proto.RedProtos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/** Local-first replacement for cloud push: an explicit foreground WebSocket connection. */
class RedConnectionService : Service() {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private var reconnectTask: ScheduledFuture<*>? = null
    private var attempts = 0
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var tokenStore: TokenStore
    private lateinit var messageStore: MessageStore
    private lateinit var socket: RedWebSocketClient

    override fun onCreate() {
        super.onCreate()
        createChannels()
        tokenStore = TokenStore(this)
        messageStore = MessageStore(this)
        socket = RedWebSocketClient(tokenStore, ::onEnvelope, ::onState)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(CONNECTION_NOTIFICATION, connectionNotification("جارٍ الاتصال…"))
        socket.connect()
        return START_STICKY
    }

    private fun onState(state: ConnectionState) {
        when (state) {
            ConnectionState.CONNECTED -> { attempts = 0; reconnectTask?.cancel(false); notifyConnection("متصل بخادم RED المحلي") }
            ConnectionState.CONNECTING -> notifyConnection("جارٍ الاتصال بخادم RED المحلي")
            ConnectionState.DISCONNECTED -> scheduleReconnect()
            ConnectionState.UNAUTHORIZED -> refreshAndReconnect()
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
                runCatching { messageStore.save(message) }.onSuccess {
                    if (message.receiverId == tokenStore.redId) {
                        socket.acknowledge(message.id, message.sequenceNumber, "DELIVERED")
                        notifyEncryptedMessage(message.senderId)
                    }
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

        fun start(context: Context) = context.startForegroundService(Intent(context, RedConnectionService::class.java))
        fun stop(context: Context) = context.stopService(Intent(context, RedConnectionService::class.java))
    }
}
