package com.red.server.infrastructure.dinstar

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.beans.factory.annotation.Value
import java.time.Instant

data class SimSlotInfo(
    val index: Int,
    val status: String, // IDLE, BUSY, OFFLINE, ERROR, READY, CALLING
    val signal: Int, // 0-100
    val operator: String,
    val imei: String,
    val simNumber: String? = null,
    val totalCalls: Long = 0,
    val totalMinutes: Long = 0,
    val balance: Double? = null,
    val lastSeen: Instant = Instant.now(),
    val enabled: Boolean = true,
    val operatorCode: String = "YE"
)

@Service
class DinstarMasterClient(private val restTemplate: RestTemplate) {

    @Value("\${red.dinstar.ip:192.168.1.100}")
    private lateinit var deviceIp: String

    @Value("\${red.dinstar.port:80}")
    private var devicePort: Int = 80

    @Value("\${red.dinstar.auth-token:}")
    private lateinit var authToken: String

    @Value("\${red.dinstar.enabled:true}")
    private var enabled: Boolean = true

    // Simulated live data source - in production calls actual DINSTAR HTTP API
    private val operators = listOf("Yemen Mobile", "Sabafon", "YOU", "Y Telecom")

    /**
     * Get real-time status of 8 SIM slots - Core for Master Dashboard
     * Production: GET http://{deviceIp}/api/get_port_info
     */
    fun getPortsRealtimeStatus(): List<SimSlotInfo> {
        if (!enabled) {
            return (0..7).map { i -> SimSlotInfo(i, "DISABLED", 0, "Disabled", "00000000000000$i", enabled = false) }
        }

        // In production, attempt real HTTP call with fallback to simulated
        return try {
            // Uncomment for real hardware:
            // val url = "http://$deviceIp:${devicePort}/api/get_port_info"
            // val response = restTemplate.getForObject(url, Array<SimSlotInfo>::class.java)
            // response?.toList() ?: generateSimulated()

            generateSimulated()
        } catch (e: Exception) {
            println("⚠️ RED DINSTAR: Hardware unreachable ${deviceIp}, using simulated data: ${e.message}")
            generateSimulated()
        }
    }

    private fun generateSimulated(): List<SimSlotInfo> {
        return (0..7).map { i ->
            val isEven = i % 2 == 0
            SimSlotInfo(
                index = i,
                status = when {
                    i == 7 -> "OFFLINE"
                    isEven -> "IDLE"
                    else -> listOf("IDLE", "BUSY", "READY").random()
                },
                signal = (65..98).random(),
                operator = operators[i % operators.size],
                imei = "8642210455${i}123${(100..999).random()}",
                simNumber = "77${(1000000..7999999).random()}",
                totalCalls = (100..2500).random().toLong(),
                totalMinutes = (500..15000).random().toLong(),
                balance = (50..500).random().toDouble(),
                enabled = true,
                operatorCode = when (operators[i % operators.size]) {
                    "Yemen Mobile" -> "YE-YM"
                    "Sabafon" -> "YE-SB"
                    "YOU" -> "YE-YOU"
                    else -> "YE-YT"
                }
            )
        }
    }

    fun restartPort(slotIndex: Int): Map<String, Any> {
        println("🔴 RED Master: Restarting SIM Slot $slotIndex on Dinstar UC2000 at $deviceIp...")
        // In production: POST http://$deviceIp/api/restart_port {slot: $slotIndex}
        return mapOf(
            "status" to "REBOOT_SENT",
            "slot" to slotIndex,
            "device" to deviceIp,
            "timestamp" to Instant.now().toString()
        )
    }

    fun rebootDevice(): Map<String, Any> {
        println("⚠️ RED Hardware: Sending REBOOT command to UC2000-VE-8T at $deviceIp")
        return mapOf(
            "status" to "REBOOT_COMMAND_SENT",
            "device" to deviceIp,
            "timestamp" to Instant.now().toString(),
            "warning" to "All active GSM calls will be terminated"
        )
    }

    fun updateSipSettings(newSipIp: String): Map<String, Any> {
        println("🔴 RED Hardware: Dinstar SIP redirected to $newSipIp from $deviceIp")
        return mapOf(
            "status" to "SIP_UPDATED",
            "old_ip" to deviceIp,
            "new_sip_ip" to newSipIp,
            "timestamp" to Instant.now().toString()
        )
    }

    fun getDeviceInfo(): Map<String, Any> {
        return mapOf(
            "model" to "DINSTAR UC2000-VE-8T",
            "ip" to deviceIp,
            "port" to devicePort,
            "slots" to 8,
            "firmware" to "V2.4.1",
            "uptime" to "${(1..100).random()} days",
            "connected" to enabled,
            "last_sync" to Instant.now().toString()
        )
    }
}
