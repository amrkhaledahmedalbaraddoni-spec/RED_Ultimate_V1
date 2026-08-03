package com.red.sovereign.features.calls

import androidx.compose.ui.graphics.Color

/**
 * RED Ultimate - Yemeni Operator Detector
 * Supports Yemen Mobile, Sabafon, YOU, Y Telecom, TeleYemen
 * System B - PSTN Gateway integration
 */

data class OperatorInfo(
    val name: String,
    val code: String,
    val brandColor: Color,
    val country: String = "YE",
    val isGsm: Boolean = true,
    val allowsPstn: Boolean = true
)

object YemeniOperatorDetector {

    private val operators = listOf(
        OperatorInfo("Yemen Mobile", "YE-YM", Color(0xFF007A33), allowsPstn = true),
        OperatorInfo("Sabafon", "YE-SB", Color(0xFFE30613), allowsPstn = true),
        OperatorInfo("YOU", "YE-YOU", Color(0xFF00AEEF), allowsPstn = true),
        OperatorInfo("Y Telecom", "YE-YT", Color(0xFFFF6600), allowsPstn = true),
        OperatorInfo("TeleYemen", "YE-TY", Color(0xFF4B0082), allowsPstn = false)
    )

    private val prefixMap = mapOf(
        // Yemen Mobile (7x)
        "77" to operators[0],
        "71" to operators[0],
        // Sabafon
        "71" to operators[1],
        "70" to operators[1],
        "72" to operators[1],
        // YOU (formerly MTN)
        "73" to operators[2],
        // Y Telecom
        "70" to operators[3],
        "77" to operators[0],
        // General fallback
        "78" to operators[2]
    )

    fun getOperatorInfo(phoneNumber: String): OperatorInfo {
        val clean = phoneNumber.replace(Regex("[^0-9]"), "")
        if (clean.length < 3) return OperatorInfo("Unknown", "UNKNOWN", Color.Gray)

        // Yemen numbers: 967 7x xxx xxxx or 7x xxx xxxx
        val normalized = when {
            clean.startsWith("967") -> clean.substring(3)
            clean.startsWith("00967") -> clean.substring(5)
            clean.startsWith("0") -> clean.substring(1)
            else -> clean
        }

        if (normalized.length < 2) return OperatorInfo("RED Local", "RED", Color(0xFFD32F2F))

        val prefix = normalized.take(2)
        val prefix3 = normalized.take(3)

        // Check 3-digit first
        when (prefix3) {
            "777", "778", "779" -> return operators[0] // Yemen Mobile
            "711", "712", "713" -> return operators[1] // Sabafon
            "733", "735", "736" -> return operators[2] // YOU
            "700", "702" -> return operators[3] // Y Telecom
        }

        return prefixMap[prefix] ?: when {
            normalized.startsWith("7") -> OperatorInfo("Yemen Mobile (Detected)", "YE-YM", Color(0xFF007A33))
            normalized.startsWith("1") || normalized.startsWith("2") || normalized.startsWith("3") -> OperatorInfo("PSTN Landline", "PSTN", Color(0xFF607D8B), isGsm = false)
            else -> OperatorInfo("RED Sovereign", "RED-SOV", Color(0xFFD32F2F), allowsPstn = false)
        }
    }

    fun isValidYemeniNumber(phoneNumber: String): Boolean {
        val clean = phoneNumber.replace(Regex("[^0-9]"), "")
        val normalized = when {
            clean.startsWith("967") -> clean.substring(3)
            clean.startsWith("0") -> clean.substring(1)
            else -> clean
        }
        return normalized.length in 9..10 && normalized.startsWith("7")
    }

    fun formatForPstn(phoneNumber: String): String {
        val info = getOperatorInfo(phoneNumber)
        val clean = phoneNumber.replace(Regex("[^0-9]"), "")
        return if (info.code.startsWith("YE") && !clean.startsWith("967")) "967$clean" else clean
    }

    fun getAllOperators(): List<OperatorInfo> = operators
}
