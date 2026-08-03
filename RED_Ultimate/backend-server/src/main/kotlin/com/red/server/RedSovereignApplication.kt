package com.red.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

@SpringBootApplication
@EnableScheduling
@EnableWebSocket
@EnableTransactionManagement
@EnableAsync
class RedSovereignApplication {
    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()

    @EventListener(ApplicationReadyEvent::class)
    fun onReady() {
        println("==========================================")
        println("🔴 RED Sovereign Ultimate V2 is ONLINE")
        println("🔴 Mode: 100% LOCAL - ZERO CLOUD")
        println("🔴 System A: VoIP 4K AV1/VP9/H264 - SFU Ready")
        println("🔴 System B: PSTN DINSTAR UC2000-VE-8T - Gateway Ready")
        println("🔴 System C: Guaranteed Delivery UUID v7 - Active")
        println("🔴 Security: POST-QUANTUM + BCrypt + JWT HS512")
        println("🔴 Storage: PostgreSQL + MongoDB + Redis + MinIO")
        println("🔴 Monitoring: Prometheus + Grafana")
        println("==========================================")
    }
}

fun main(args: Array<String>) {
    System.setProperty("spring.main.banner-mode", "off")
    runApplication<RedSovereignApplication>(*args)
}
