package com.red.sovereign.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val api = AuthApi()
    private val tokens = TokenStore(application)
    private val keys = DeviceKeyManager(application)
    private val pstn = PstnApi(tokens)

    var pstnState: PstnState by mutableStateOf(PstnState.Idle)
        private set

    var state: AuthState by mutableStateOf(AuthState.Loading)
        private set
    private var pendingCredentials: Pair<String, String>? = null

    init { restore() }

    fun showRegister() { state = AuthState.Register }
    fun showLogin() { state = AuthState.Login }
    fun showWelcome() { state = AuthState.Welcome }

    fun register(displayName: String, username: String, password: String) = viewModelScope.launch {
        state = AuthState.Submitting
        val enrollment = runCatching { withContext(Dispatchers.Default) { keys.enrollment() } }
            .getOrElse { state = AuthState.Error("تعذر إنشاء مفاتيح التشفير المحلية"); return@launch }
        when (val result = api.register(RegisterRequest(username.trim(), password, displayName.trim(), enrollment))) {
            is ApiResult.Success -> {
                result.value.deviceId?.let(tokens::rememberDevice)
                pendingCredentials = username.trim() to password
                state = AuthState.Pending(result.value.user.redId, result.value.user.username)
            }
            is ApiResult.Error -> state = AuthState.Error(localize(result.message))
        }
    }

    fun login(username: String, password: String) = viewModelScope.launch {
        state = AuthState.Submitting
        when (val result = api.login(LoginRequest(username.trim(), password, tokens.deviceId))) {
            is ApiResult.Error -> state = AuthState.Error(localize(result.message))
            is ApiResult.Success -> applyAuth(result.value, username, password)
        }
    }

    fun checkApproval() {
        val credentials = pendingCredentials
        if (credentials == null) state = AuthState.Login
        else login(credentials.first, credentials.second)
    }

    fun dialPstn(number: String) = viewModelScope.launch {
        pstnState = PstnState.Dialing
        pstnState = when (val result = pstn.dial(number)) {
            is ApiResult.Success -> PstnState.Started(result.value.callId, result.value.usedToday, result.value.dailyLimit)
            is ApiResult.Error -> PstnState.Error(localize(result.message))
        }
    }

    fun clearPstnState() { pstnState = PstnState.Idle }

    fun logout() {
        tokens.clearSession()
        pendingCredentials = null
        state = AuthState.Welcome
    }

    private fun restore() = viewModelScope.launch {
        val refresh = tokens.refreshToken
        if (refresh == null) { state = AuthState.Welcome; return@launch }
        when (val result = api.refresh(refresh)) {
            is ApiResult.Success -> {
                tokens.updateTokens(result.value)
                state = AuthState.Authenticated(tokens.redId.orEmpty(), tokens.username.orEmpty(), tokens.pstnEnabled)
            }
            is ApiResult.Error -> { tokens.clearSession(); state = AuthState.Welcome }
        }
    }

    private fun applyAuth(response: AuthResponse, username: String, password: String) {
        when (response.status) {
            "APPROVED" -> {
                tokens.save(response)
                pendingCredentials = null
                state = AuthState.Authenticated(response.user.redId, response.user.username, response.user.pstnEnabled)
            }
            "PENDING" -> {
                pendingCredentials = username to password
                state = AuthState.Pending(response.user.redId, response.user.username)
            }
            "REJECTED" -> state = AuthState.Rejected(response.user.rejectionReason)
            "SUSPENDED" -> state = AuthState.Suspended
            "BANNED" -> state = AuthState.Banned
            else -> state = AuthState.Error("حالة حساب غير معروفة")
        }
    }

    private fun localize(value: String) = when (value) {
        "INVALID_CREDENTIALS" -> "اسم المستخدم أو كلمة المرور غير صحيحة"
        "NETWORK_ERROR" -> "تعذر الاتصال بخادم RED المحلي"
        else -> value
    }
}

sealed interface AuthState {
    data object Loading : AuthState
    data object Welcome : AuthState
    data object Register : AuthState
    data object Login : AuthState
    data object Submitting : AuthState
    data class Pending(val redId: String, val username: String) : AuthState
    data class Authenticated(val redId: String, val username: String, val pstnEnabled: Boolean) : AuthState
    data class Rejected(val reason: String?) : AuthState
    data object Suspended : AuthState
    data object Banned : AuthState
    data class Error(val message: String) : AuthState
}

sealed interface PstnState {
    data object Idle : PstnState
    data object Dialing : PstnState
    data class Started(val callId: String, val usedToday: Int, val dailyLimit: Int) : PstnState
    data class Error(val message: String) : PstnState
}
