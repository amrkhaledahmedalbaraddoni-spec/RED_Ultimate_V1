package com.red.core.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer

@Configuration
class RedisConfig {

    /**
     * Typing Indicator & Online Status (System C)
     * Using Redis Pub/Sub for sub-1ms latency.
     */
    @Bean
    fun redisContainer(
        connectionFactory: RedisConnectionFactory,
        messageListener: RedisMessageSubscriber
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        container.addMessageListener(messageListener, PatternTopic("chat:typing:*"))
        container.addMessageListener(messageListener, PatternTopic("user:status:*"))
        return container
    }

    @Bean
    fun redisTemplate(factory: RedisConnectionFactory): StringRedisTemplate {
        return StringRedisTemplate(factory)
    }
}
