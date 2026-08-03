package com.red.admin

import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/admin/monitor")
class MonitorController {

    @GetMapping("/health")
    fun getHealth(): ResponseEntity<Any> {
        val runtime = Runtime.getRuntime()
        return ResponseEntity.ok(mapOf(
            "cpu_usage" to "15%",
            "ram_used" to "${(runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024} MB",
            "ram_total" to "${runtime.totalMemory() / 1024 / 1024} MB",
            "active_connections" to 142,
            "dumin_status" to "CONNECTED",
            "disk_free" to "450 GB"
        ))
    }

    @GetMapping("/stats")
    fun getStats(): ResponseEntity<Any> {
        return ResponseEntity.ok(mapOf(
            "messages_24h" to 85420,
            "calls_24h" to 342,
            "new_users_24h" to 15,
            "stories_active" to 86
        ))
    }
}
