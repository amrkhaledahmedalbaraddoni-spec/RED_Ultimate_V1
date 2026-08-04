package com.red.server.controllers

import com.red.server.services.DinstarHardwareService
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/admin/dinstar")
class DinstarController(private val dinstarService: DinstarHardwareService) {

    /**
     * جلب الحالة الحية للـ 8 منافذ (للمدير وللتطبيق)
     */
    @GetMapping("/status")
    fun getStatus(): ResponseEntity<Any> {
        val status = dinstarService.getHardwareStatus()
        return ResponseEntity.ok(status)
    }

    /**
     * أمر إعادة تشغيل الهاردوير
     */
    @PostMapping("/reboot")
    fun reboot(): ResponseEntity<Any> {
        dinstarService.rebootDevice()
        return ResponseEntity.ok(mapOf("status" to "REBOOT_COMMAND_SENT"))
    }

    /**
     * تعديل إعدادات SIP Trunk يدوياً
     */
    @PostMapping("/config/sip")
    fun updateSip(@RequestBody data: Map<String, String>): ResponseEntity<Any> {
        val newIp = data["sip_ip"] ?: return ResponseEntity.badRequest().build()
        dinstarService.updateSipSettings(newIp)
        return ResponseEntity.ok(mapOf("status" to "SUCCESS"))
    }

    /**
     * بدء مكالمة PSTN عبر خط Dinstar من تطبيق الأندرويد
     */
    @PostMapping("/dial")
    fun dialNumber(@RequestBody body: Map<String, Any>): ResponseEntity<Any> {
        val number = body["number"] as? String ?: return ResponseEntity.badRequest().body(mapOf("error" to "Number required"))
        val slot = (body["slot"] as? Number)?.toInt() ?: 0
        val result = dinstarService.initiateCall(number, slot)
        return ResponseEntity.ok(result)
    }
}
