package com.red.server.controllers

import com.red.server.auth.ApprovalActionRequest
import com.red.server.auth.RedApprovalService
import com.red.server.services.CoreService
import com.red.server.services.RedSecurityService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val approvalService: RedApprovalService,
    private val coreService: CoreService,
    private val securityService: RedSecurityService
) {
    @GetMapping("/users/pending")
    fun getPendingUsers() = ResponseEntity.ok(approvalService.getPendingList())

    @PostMapping("/users/action")
    fun updateUserStatus(
        @RequestBody request: ApprovalActionRequest,
        authentication: Authentication
    ) = ResponseEntity.ok(
        approvalService.processAction(
            userId = request.userId,
            action = request.action,
            reason = request.reason,
            adminId = UUID.fromString(authentication.name)
        )
    )

    @PostMapping("/users/update-status")
    fun legacyUpdateUserStatus(
        @RequestParam userId: String,
        @RequestParam status: String,
        authentication: Authentication
    ) = ResponseEntity.ok(
        approvalService.processAction(
            UUID.fromString(userId),
            com.red.server.auth.model.AccountStatus.valueOf(status.uppercase()),
            adminId = UUID.fromString(authentication.name)
        )
    )

    @GetMapping("/stories/monitor")
    fun monitorStories() = ResponseEntity.ok(coreService.getActiveStoriesCount())

    @PostMapping("/security/wipe")
    fun wipeUser(@RequestParam userId: String) =
        ResponseEntity.ok(securityService.sendWipeSignal(userId))

    @PostMapping("/security/kill-switch")
    fun activateKillSwitch(@RequestParam reason: String) =
        ResponseEntity.ok(securityService.activateKillSwitch(reason))
}
