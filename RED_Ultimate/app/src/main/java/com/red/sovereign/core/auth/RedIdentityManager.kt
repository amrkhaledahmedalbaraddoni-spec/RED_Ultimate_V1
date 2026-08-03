package com.red.sovereign.core.auth

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedIdentityManager @Inject constructor(
    private val prefs: SharedPreferences
) : IdentityManager {

    companion object {
        private const val KEY_RED_ID = "RED_ID"
        private const val KEY_GSM_NUMBER = "GSM_NUMBER"
        private const val KEY_AUTH_TOKEN = "AUTH_TOKEN"
        private const val KEY_IS_APPROVED = "IS_APPROVED"
        private const val KEY_USER_HANDLE = "USER_HANDLE"
        private const val KEY_USER_NAME = "USER_NAME"
        private const val KEY_EMAIL = "EMAIL"
    }

    fun finalizeIdentity(redId: String, gsmNumber: String, token: String) {
        prefs.edit().apply {
            putString(KEY_RED_ID, redId)
            putString(KEY_GSM_NUMBER, gsmNumber)
            putString(KEY_AUTH_TOKEN, token)
            putBoolean(KEY_IS_APPROVED, true)
            putString(KEY_USER_HANDLE, redId)
            apply()
        }
    }

    fun setUserInfo(email: String, name: String, handle: String) {
        prefs.edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_HANDLE, handle)
            apply()
        }
    }

    override fun getRedId(): String = prefs.getString(KEY_RED_ID, "") ?: ""

    override fun getUserHandle(): String = prefs.getString(KEY_USER_HANDLE, getRedId()) ?: getRedId()

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "RED User") ?: "RED User"

    override fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)

    fun getGsmNumber(): String = prefs.getString(KEY_GSM_NUMBER, "") ?: ""

    fun getEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""

    override fun isLoggedIn(): Boolean = isApproved() && !getAuthToken().isNullOrEmpty()

    fun isApproved(): Boolean = prefs.getBoolean(KEY_IS_APPROVED, false)

    override fun logout() {
        prefs.edit().clear().apply()
    }

    fun clearAll() = logout()
}
