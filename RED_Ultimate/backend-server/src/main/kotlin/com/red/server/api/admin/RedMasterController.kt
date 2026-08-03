package com.red.server.api.admin

import com.red.server.services.*
import com.red.server.auth.RedApprovalService
import com.red.server.infrastructure.dinstar.DinstarMasterClient
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/master/v1")
class RedMasterController(
    private val statsService: MasterStatsService,
    private val approvalService: RedApprovalService,
    private val dinstarClient: DinstarMasterClient,
    private val securityService: RedSecurityService,
    private val coreService: CoreService
) {
    @GetMapping("/stats/realtime")
    fun getGlobalStats(): ResponseEntity<Any> {
        return ResponseEntity.ok(statsService.getRealtimeForAdmin())
    }

    @GetMapping("/stats/overview")
    fun getOverview(): ResponseEntity<Any> {
        return ResponseEntity.ok(statsService.getLiveMetrics())
    }

    @GetMapping("/auth/pending")
    fun listPending() = ResponseEntity.ok(approvalService.getPendingList())

    @GetMapping("/auth/all")
    fun listAll() = ResponseEntity.ok(approvalService.getAllUsers())

    @PostMapping("/auth/action")
    fun handleUserAction(@RequestBody req: Map<String, String>): ResponseEntity<Any> {
        val userId = req["userId"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "userId required"))
        val action = req["action"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "action required"))
        return ResponseEntity.ok(approvalService.processAction(userId, action))
    }

    @PostMapping("/auth/approve/{userId}")
    fun approveById(@PathVariable userId: String) = ResponseEntity.ok(approvalService.approveUser(userId))

    @PostMapping("/auth/ban/{userId}")
    fun banById(@PathVariable userId: String) = ResponseEntity.ok(approvalService.banUser(userId))

    @GetMapping("/hardware/dinstar/slots")
    fun getSlots() = ResponseEntity.ok(dinstarClient.getPortsRealtimeStatus())

    @GetMapping("/hardware/dinstar/info")
    fun getInfo() = ResponseEntity.ok(dinstarClient.getDeviceInfo())

    @PostMapping("/hardware/dinstar/restart/{slot}")
    fun restartSlot(@PathVariable slot: Int) = ResponseEntity.ok(dinstarClient.restartPort(slot))

    @PostMapping("/security/wipe")
    fun initiateWipe(@RequestParam userId: String) = ResponseEntity.ok(securityService.triggerKillSwitch(userId))

    @GetMapping("/media/active-calls")
    fun getActiveCalls() = ResponseEntity.ok(statsService.getVoipMetrics())

    @GetMapping("/system/groups")
    fun getGroups() = ResponseEntity.ok(coreService.getAllGroups())

    @GetMapping("/system/stories")
    fun getStories() = ResponseEntity.ok(coreService.getActiveStories())
}
