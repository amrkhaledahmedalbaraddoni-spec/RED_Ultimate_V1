package com.red.server.services

import com.red.server.infrastructure.dinstar.DinstarMasterClient
import org.springframework.stereotype.Service

@Service
class DinstarHardwareService(
    private val masterClient: DinstarMasterClient
) {
    fun getHardwareStatus(): List<Map<String, Any>> {
        val slots = masterClient.getPortsRealtimeStatus()
        return slots.map { s ->
            mapOf(
                "index" to s.index,
                "slot" to s.index,
                "status" to s.status,
                "signal" to s.signal,
                "operator" to s.operator,
                "imei" to s.imei,
                "simNumber" to (s.simNumber ?: ""),
                "balance" to (s.balance ?: 0.0),
                "enabled" to s.enabled
            )
        }
    }

    fun getDetailedStatus(): Map<String, Any> {
        return mapOf(
            "device" to masterClient.getDeviceInfo(),
            "slots" to getHardwareStatus(),
            "total_slots" to 8,
            "active_calls" to getHardwareStatus().count { it["status"] == "BUSY" },
            "online_slots" to getHardwareStatus().count { it["status"] != "OFFLINE" && it["status"] != "DISABLED" }
        )
    }

    fun updateSipSettings(newSipIp: String): Map<String, Any> {
        return masterClient.updateSipSettings(newSipIp)
    }

    fun rebootDevice(): Map<String, Any> {
        return masterClient.rebootDevice()
    }

    fun restartSlot(slotIndex: Int): Map<String, Any> {
        return masterClient.restartPort(slotIndex)
    }
}
