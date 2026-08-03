package com.red.sovereign.features.pstn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.sovereign.features.calls.YemeniOperatorDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PstnViewModel @Inject constructor() : ViewModel() {

    data class GsmCall(
        val slotIndex: Int,
        val number: String,
        val operator: String?,
        val status: String = "IDLE", // IDLE, DIALING, CONNECTED, ENDED
        val duration: Long = 0,
        val startedAt: Long = System.currentTimeMillis()
    )

    private val _activeCall = MutableStateFlow<GsmCall?>(null)
    val activeCall: StateFlow<GsmCall?> = _activeCall

    private val _gatewayStatus = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val gatewayStatus: StateFlow<List<Map<String, Any>>> = _gatewayStatus

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    fun dialPstn(number: String, slot: Int = 0) {
        val operator = YemeniOperatorDetector.getOperatorInfo(number)
        _activeCall.value = GsmCall(slot, number, operator.name, "DIALING")
        viewModelScope.launch {
            // Simulate API call to backend /api/admin/dinstar/status and POST call
            // In production: POST /api/pstn/call {number, slot}
        }
    }

    fun endGsmCall() {
        _activeCall.value = _activeCall.value?.copy(status = "ENDED")
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            _activeCall.value = null
        }
    }

    fun syncGatewayStatus() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                // Simulate fetch from /api/admin/dinstar/status
                // Real implementation would use Retrofit
                _gatewayStatus.value = (0..7).map { i ->
                    mapOf(
                        "index" to i,
                        "status" to if (i % 2 == 0) "IDLE" else "READY",
                        "signal" to (70..95).random(),
                        "operator" to YemeniOperatorDetector.getOperatorInfo("77${i}").name
                    )
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun getActiveCall(): GsmCall? = _activeCall.value

    fun prepare() {
        syncGatewayStatus()
    }
}
