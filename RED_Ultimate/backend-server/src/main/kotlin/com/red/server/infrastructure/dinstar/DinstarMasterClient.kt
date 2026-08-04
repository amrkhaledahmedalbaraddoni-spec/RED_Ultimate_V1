package com.red.server.infrastructure.dinstar

import com.red.server.services.DinstarHardwareService
import org.springframework.stereotype.Service

/**
 * Compatibility facade for dashboard endpoints. All values come from the real
 * authenticated DINSTAR hardware service; no generated signal, operator or IMEI data.
 */
@Service
class DinstarMasterClient(private val hardware: DinstarHardwareService) {
    fun getPortsRealtimeStatus(): List<Map<String, Any>> = hardware.getHardwareStatus()

    fun restartPort(slotIndex: Int): Nothing =
        throw UnsupportedOperationException(
            "The configured DINSTAR API does not expose a verified per-port restart operation (slot $slotIndex)"
        )
}
