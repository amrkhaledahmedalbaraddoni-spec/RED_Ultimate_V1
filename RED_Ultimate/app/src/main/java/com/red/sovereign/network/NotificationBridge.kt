package com.red.sovereign.network

import com.red.sovereign.core.crypto.QuantumGuard
import com.red.sovereign.core.delivery.MasterDeliveryEngine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationBridge @Inject constructor(
    private val deliveryEngine: MasterDeliveryEngine,
    private val quantumGuard: QuantumGuard? = null
) {
    fun processIncomingRED(payload: ByteArray) {
        try {
            val unwrapped = quantumGuard?.unwrapQuantum(payload) ?: payload
            val text = String(unwrapped)
            // If JSON, dispatch
            if (text.contains("conversationId")) {
                // Extract conversation
                // Simplified parse
                val convId = Regex("\"conversationId\":\"([^\"]+)\"").find(text)?.groupValues?.get(1) ?: "global"
                val content = Regex("\"content\":\"([^\"]+)\"").find(text)?.groupValues?.get(1) ?: text
                deliveryEngine.dispatchMessage(convId, content)
            }
            println("🔴 RED Bridge: Processing sovereign signal ${unwrapped.size} bytes")
        } catch (e: Exception) {
            println("⚠️ RED Bridge error: ${e.message}")
        }
    }

    fun processIncomingREDText(json: String) {
        processIncomingRED(json.toByteArray())
    }
}
