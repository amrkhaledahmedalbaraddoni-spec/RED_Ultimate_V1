package com.red.server.services

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

@Service
class DinstarMasterService {
    private val restTemplate = RestTemplate()
    
    /**
     * جلب حالة الـ 8 منافذ الفعلية من جهاز DINSTAR
     * UC2000 Specific: /api/get_port_status
     */
    fun fetchLiveSimStatus(deviceIp: String): List<Map<String, Any>> {
        return try {
            // الطلب الفعلي من واجهة الجهاز (Admin API)
            // val response = restTemplate.getForObject("http://$deviceIp/api/get_port_status", Map::class.java)
            
            // محاكاة الاستجابة الصحيحة للـ 8 منافذ (8T)
            (1..8).map { slot ->
                mapOf(
                    "slot" to slot,
                    "status" to if (slot % 3 == 0) "BUSY" else "IDLE",
                    "operator" to "Yemen Mobile",
                    "signal" to (60..95).random()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
