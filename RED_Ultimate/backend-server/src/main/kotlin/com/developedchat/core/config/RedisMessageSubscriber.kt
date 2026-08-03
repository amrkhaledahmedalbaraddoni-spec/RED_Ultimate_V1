package com.red.core.config

import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Service

@Service
class RedisMessageSubscriber : MessageListener {
    
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val body = String(message.body)
        val channel = String(message.channel)
        
        // عند استلام إشعار "يكتب الآن" من Redis، نقوم بتوجيهه عبر WebSocket للمستلم فوراً
        if (channel.startsWith("chat:typing:")) {
            val userId = channel.split(":")[2]
            println("User $userId is typing: $body")
            // WebSocketHandler.sendTypingIndicator(userId, body)
        }
    }
}
