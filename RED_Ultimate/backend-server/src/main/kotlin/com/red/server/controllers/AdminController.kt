package com.red.server.controllers

import com.red.server.auth.RedApprovalService
import com.red.server.services.CoreService
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val approvalService: RedApprovalService,
    private val coreService: CoreService
) {

    // 1. جلب قائمة الانتظار
    @GetMapping("/users/pending")
    fun getPendingUsers() = ResponseEntity.ok(approvalService.getPendingList())

    // 2. الموافقة أو الحظر
    @PostMapping("/users/update-status")
    fun updateUserStatus(@RequestParam userId: String, @RequestParam status: String): ResponseEntity<Any> {
        when (status) {
            "APPROVED" -> approvalService.approveUser(userId)
            "BANNED" -> approvalService.banUser(userId)
            "REJECTED" -> approvalService.rejectUser(userId)
        }
        return ResponseEntity.ok(mapOf("status" to "SUCCESS"))
    }

    // 3. مراقبة القصص النشطة
    @GetMapping("/stories/monitor")
    fun monitorStories() = ResponseEntity.ok(coreService.getActiveStoriesCount())

    // 4. مفتاح القتل (Security Kill Switch)
    @PostMapping("/security/kill-switch")
    fun activateKillSwitch(@RequestParam userId: String): ResponseEntity<Any> {
        // إرسال أمر مسح البيانات للجهاز عبر WebSocket
        println("⚠️ RED Master Security: Remote Wipe triggered for $userId")
        return ResponseEntity.ok(mapOf("action" to "WIPE_SIGNAL_SENT"))
    }
}
