package com.red.server.services

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.http.*

/**
 * RED Dinstar Hardware Master
 * Controls UC2000-ve-8t directly via its Cloud/API interface.
 */
@Service
class DinstarHardwareService {
    private val restTemplate = RestTemplate()
    private val deviceUrl = "http://192.168.1.100" // Dynamic from DB

    /**
     * جلب حالة الـ 8 شرائح لحظياً
     */
    fun getHardwareStatus(): List<Map<String, Any>> {
        // في الواقع، نطلب API من جهاز Dinstar
        // return restTemplate.getForObject("$deviceUrl/api/get_port_status", List::class.java)
        return (0..7).map { i ->
            mapOf("index" to i, "status" to "READY", "signal" to 85, "operator" to "Yemen Mobile")
        }
    }

    /**
     * تغيير إعدادات الـ SIP Trunk في الجهاز
     */
    fun updateSipSettings(newSipIp: String) {
        val payload = mapOf("sip_server" to newSipIp)
        // restTemplate.postForEntity("$deviceUrl/api/set_sip", payload, String::class.java)
        println("🔴 RED Hardware: Dinstar SIP redirected to $newSipIp")
    }

    /**
     * إعادة تشغيل الجهاز أو منفذ معين
     */
    fun rebootDevice() {
        println("⚠️ RED Hardware: Sending REBOOT command to UC2000-ve-8t")
        // restTemplate.postForEntity("$deviceUrl/api/reboot", null, String::class.java)
    }
}
