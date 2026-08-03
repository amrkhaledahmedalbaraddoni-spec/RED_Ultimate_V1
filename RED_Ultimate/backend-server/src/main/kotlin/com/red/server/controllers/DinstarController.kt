package com.red.server.controllers

import com.red.server.services.DinstarHardwareService
import com.red.server.infrastructure.dinstar.DinstarMasterClient
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/admin/dinstar")
class DinstarController(
    private val dinstarService: DinstarHardwareService,
    private val dinstarClient: DinstarMasterClient
) {
    @GetMapping("/status")
    fun getStatus(): ResponseEntity<Any> {
        return ResponseEntity.ok(dinstarClient.getPortsRealtimeStatus())
    }

    @GetMapping("/slots")
    fun getSlots(): ResponseEntity<Any> = getStatus()

    @PostMapping("/reboot")
    fun reboot(): ResponseEntity<Any> {
        val result = dinstarClient.rebootDevice()
        return ResponseEntity.ok(result)
    }

    @PostMapping("/restart/{slot}")
    fun restartSlot(@PathVariable slot: Int): ResponseEntity<Any> {
        val result = dinstarClient.restartPort(slot)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/config/sip")
    fun updateSip(@RequestBody data: Map<String, String>): ResponseEntity<Any> {
        val newIp = data["sip_ip"] ?: data["sip_server"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "sip_ip required"))
        return ResponseEntity.ok(dinstarClient.updateSipSettings(newIp))
    }

    @GetMapping("/info")
    fun getInfo(): ResponseEntity<Any> {
        return ResponseEntity.ok(dinstarClient.getDeviceInfo())
    }

    // Legacy compat for Master tabs
    @GetMapping("/hardware/status")
    fun hardwareStatus() = getStatus()
}
