package com.red.sovereign.crypto

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.red.sovereign.auth.ApiResult
import com.red.sovereign.auth.AuthorizedApiClient
import com.red.sovereign.auth.TokenStore
import com.red.sovereign.core.SecureStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Base64

class SafetyViewModel(application: Application) : AndroidViewModel(application) {
    private val tokens = TokenStore(application)
    private val directory = IdentityDirectoryApi(AuthorizedApiClient(tokens))
    private val sessions = SignalSessionManager(application)
    private val verified = SecureStore(application, "younes_safety_verification")
    var state: SafetyState by mutableStateOf(SafetyState.Closed); private set

    fun open(remoteRedId: String) = viewModelScope.launch {
        state = SafetyState.Loading(remoteRedId)
        when (val result = directory.get(remoteRedId)) {
            is ApiResult.Error -> state = SafetyState.Error(remoteRedId, result.message)
            is ApiResult.Success -> {
                val device = result.value.devices.firstOrNull()
                if (device == null) { state = SafetyState.Error(remoteRedId, "NO_APPROVED_DEVICE"); return@launch }
                val number = sessions.safetyNumber(remoteRedId, Base64.getDecoder().decode(device.identityKey))
                val key = verificationKey(remoteRedId, device.protocolDeviceId)
                val isVerified = verified.get(key) == device.identityFingerprint
                val payload = listOf("younes-safety-v1", tokens.redId.orEmpty(), remoteRedId, device.protocolDeviceId, device.identityFingerprint, number.replace(" ", "")).joinToString("|")
                val qr = withContext(Dispatchers.Default) { qrBitmap(payload) }
                state = SafetyState.Ready(remoteRedId, device.protocolDeviceId, device.identityFingerprint, number, qr, isVerified)
            }
        }
    }

    fun markVerified() {
        val current = state as? SafetyState.Ready ?: return
        verified.put(verificationKey(current.remoteRedId, current.deviceId), current.fingerprint)
        state = current.copy(verified = true)
    }

    fun close() { state = SafetyState.Closed }

    private fun verificationKey(redId: String, deviceId: Int) = "$redId:$deviceId"

    private fun qrBitmap(payload: String): ImageBitmap {
        val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 640, 640)
        val pixels = IntArray(matrix.width * matrix.height) { index ->
            if (matrix[index % matrix.width, index / matrix.width]) 0xff06101c.toInt() else 0xffffffff.toInt()
        }
        return Bitmap.createBitmap(pixels, matrix.width, matrix.height, Bitmap.Config.ARGB_8888).asImageBitmap()
    }
}

sealed interface SafetyState {
    data object Closed : SafetyState
    data class Loading(val remoteRedId: String) : SafetyState
    data class Ready(
        val remoteRedId: String,
        val deviceId: Int,
        val fingerprint: String,
        val number: String,
        val qr: ImageBitmap,
        val verified: Boolean
    ) : SafetyState
    data class Error(val remoteRedId: String, val message: String) : SafetyState
}
