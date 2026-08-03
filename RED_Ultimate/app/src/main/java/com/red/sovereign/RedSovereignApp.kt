package com.red.sovereign

import android.app.Application
import com.red.sovereign.core.delivery.MasterDeliveryEngine
import com.red.sovereign.features.calls.RedVoipMaster
import com.red.sovereign.core.auth.IdentityManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RedSovereignApp : Application() {
    @Inject lateinit var deliveryEngine: MasterDeliveryEngine
    @Inject lateinit var voipMaster: RedVoipMaster
    @Inject lateinit var identityManager: IdentityManager

    override fun onCreate() {
        super.onCreate()
        // 1. فرض بيئة الأرقام اللاتينية لمنع أخطاء (١٢٣)
        java.util.Locale.setDefault(java.util.Locale.US)
        
        // 2. تشغيل المحركات السيادية فوراً
        if (identityManager.isLoggedIn()) {
            deliveryEngine.initialize()
            voipMaster.prepare()
        }
    }
}
