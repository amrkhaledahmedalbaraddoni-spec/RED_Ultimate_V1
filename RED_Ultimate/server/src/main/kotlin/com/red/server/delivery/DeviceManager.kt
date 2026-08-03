package com.red.server.delivery

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * RED Device Manager
 * Handles multi-device synchronization for a single user account.
 */
@Service
class DeviceManager {
    // تخزين الأجهزة النشطة لكل مستخدم: UserId -> Set<DeviceId>
    private val userDevices = ConcurrentHashMap<String, MutableSet<String>>()

    fun registerDevice(userId: String, deviceId: String) {
        userDevices.computeIfAbsent(userId) { mutableSetOf() }.add(deviceId)
        println("🔴 RED: Device $deviceId registered for user $userId")
    }

    fun getActiveDevices(userId: String): Set<String> {
        return userDevices[userId] ?: emptySet()
    }

    fun removeDevice(userId: String, deviceId: String) {
        userDevices[userId]?.remove(deviceId)
    }
}
