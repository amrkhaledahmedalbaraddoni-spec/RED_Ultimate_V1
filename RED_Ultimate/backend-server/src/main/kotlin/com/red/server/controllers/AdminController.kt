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
    @GetMapping("/users/pending")
    fun getPendingUsers() = ResponseEntity.ok(approvalService.getPendingList())

    @GetMapping("/users/approved")
    fun getApproved() = ResponseEntity.ok(approvalService.getApprovedUsers())

    @GetMapping("/users/all")
    fun getAll() = ResponseEntity.ok(approvalService.getAllUsers())

    @GetMapping("/users/stats")
    fun getStats() = ResponseEntity.ok(approvalService.getStats())

    @PostMapping("/users/update-status")
    fun updateUserStatus(@RequestParam userId: String, @RequestParam status: String): ResponseEntity<Any> {
        val result = approvalService.processAction(userId, status)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/users/approve/{userId}")
    fun approve(@PathVariable userId: String) = ResponseEntity.ok(approvalService.approveUser(userId))

    @PostMapping("/users/ban/{userId}")
    fun ban(@PathVariable userId: String) = ResponseEntity.ok(approvalService.banUser(userId))

    @PostMapping("/users/reject/{userId}")
    fun reject(@PathVariable userId: String) = ResponseEntity.ok(approvalService.rejectUser(userId))

    @GetMapping("/stories/monitor")
    fun monitorStories() = ResponseEntity.ok(coreService.getActiveStoriesCount())

    @GetMapping("/stories/active")
    fun activeStories() = ResponseEntity.ok(coreService.getActiveStories())

    @GetMapping("/groups")
    fun groups() = ResponseEntity.ok(coreService.getAllGroups())

    @PostMapping("/security/kill-switch")
    fun activateKillSwitch(@RequestParam userId: String): ResponseEntity<Any> {
        println("⚠️ RED Master Security: Remote Wipe triggered for $userId")
        return ResponseEntity.ok(mapOf("action" to "WIPE_SIGNAL_SENT", "userId" to userId, "timestamp" to System.currentTimeMillis()))
    }

    @PostMapping("/security/kill-switch/{userId}")
    fun killSwitchById(@PathVariable userId: String): ResponseEntity<Any> {
        return activateKillSwitch(userId)
    }
}
