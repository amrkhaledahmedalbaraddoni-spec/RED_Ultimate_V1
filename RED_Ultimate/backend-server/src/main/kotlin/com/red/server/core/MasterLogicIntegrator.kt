package com.red.server.core

import com.red.server.api.admin.RedMasterController
import com.red.server.websocket.RedMasterHandler
import com.red.server.pstn.DinstarMasterService
import com.red.server.services.CoreService

/**
 * RED Backend Master Logic
 * يربط (الرسائل، المزامنة، Dinstar، الإدارة) في محرك واحد
 */
class MasterLogicIntegrator(
    val handler: RedMasterHandler,       // تدفق ProtoBuf
    val gateway: DinstarMasterService,   // التحكم بـ UC2000
    val auth: RedMasterController,       // الموافقة الإدارية
    val stories: CoreService             // الحذف التلقائي للقصص
) {
    fun status() = "🟢 RED SERVER: All sovereign logics are active."
}
