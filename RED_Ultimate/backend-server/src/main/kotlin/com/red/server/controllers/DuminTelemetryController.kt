package com.red.server.controllers

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/dumin")
class DuminTelemetryController(
    @Value("\${red.dinstar.ip:192.168.1.100}") private val dinstarIp: String
) {
    @GetMapping("/telemetry")
    fun getTelemetry(): Map<String, Any> {
        return mapOf(
            "gateway_ip" to dinstarIp,
            "status" to "ONLINE",
            "total_ports" to 8,
            "active_ports" to 2,
            "signal_quality" to 85,
            "registered_lines" to listOf(
                mapOf("port" to 1, "number" to "+96771XXXX01", "operator" to "Yemen Mobile", "status" to "REGISTERED"),
                mapOf("port" to 2, "number" to "+96771XXXX02", "operator" to "Sabafon", "status" to "REGISTERED"),
                mapOf("port" to 3, "number" to "", "operator" to "", "status" to "IDLE"),
                mapOf("port" to 4, "number" to "", "operator" to "", "status" to "IDLE"),
                mapOf("port" to 5, "number" to "", "operator" to "", "status" to "IDLE"),
                mapOf("port" to 6, "number" to "", "operator" to "", "status" to "IDLE"),
                mapOf("port" to 7, "number" to "", "operator" to "", "status" to "IDLE"),
                mapOf("port" to 8, "number" to "", "operator" to "", "status" to "IDLE")
            ),
            "total_calls_today" to 15,
            "total_sms_today" to 42,
            "uptime_hours" to 168,
            "timestamp" to System.currentTimeMillis()
        )
    }
}
