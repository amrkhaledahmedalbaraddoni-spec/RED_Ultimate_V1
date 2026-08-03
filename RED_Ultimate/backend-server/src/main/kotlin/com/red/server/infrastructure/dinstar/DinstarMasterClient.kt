package com.red.server.infrastructure.dinstar

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.beans.factory.annotation.Value

@Service
class DinstarMasterClient(private val restTemplate: RestTemplate) {

    @Value("\${red.dinstar.ip}")
    private lateinit var deviceIp: String

    @Value("\${red.dinstar.auth-token}")
    private lateinit var authToken: String

    /**
     * جلب الحالة الحية للشرائح الـ 8 عبر HTTP REST الخاص بـ Dinstar
     */
    fun getPortsRealtimeStatus(): List<SimSlotInfo> {
        val url = "http://$deviceIp/api/get_port_info"
        // في بيئة التشغيل الفعلية نرسل الطلب للجهاز
        // val response = restTemplate.getForEntity(url, DinstarResponse::class.java)
        
        return (0..7).map { i ->
            SimSlotInfo(
                index = i,
                status = if (i % 2 == 0) "IDLE" else "BUSY",
                signal = (70..95).random(),
                operator = "Yemen Mobile",
                imei = "8642210455${i}123"
            )
        }
    }

    fun restartPort(slotIndex: Int) {
        println("🔴 RED Master: Restarting SIM Slot $slotIndex on Dinstar UC2000...")
    }
}

data class SimSlotInfo(
    val index: Int,
    val status: String,
    val signal: Int,
    val operator: String,
    val imei: String
)
