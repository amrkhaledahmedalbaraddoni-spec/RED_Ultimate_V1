package com.red.server.api

import com.red.server.infrastructure.dinstar.DinstarMasterClient
import com.red.server.auth.RedApprovalService
import com.red.server.services.CoreService
import com.red.server.services.MasterStatsService
import com.red.server.services.RedSecurityService
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/master/admin")
class AdminMasterController(
    private val dinstar: DinstarMasterClient,
    private val approval: RedApprovalService,
    private val core: CoreService,
    private val stats: MasterStatsService,
    private val security: RedSecurityService? = null
) {
    @GetMapping("/system/stats")
    fun getGlobalStats() = ResponseEntity.ok(stats.getLiveMetrics())

    @GetMapping("/stats/realtime")
    fun getRealtime() = ResponseEntity.ok(stats.getRealtimeForAdmin())

    @GetMapping("/hardware/dinstar/slots")
    fun getDinstarSlots() = ResponseEntity.ok(dinstar.getPortsRealtimeStatus())

    @PostMapping("/hardware/dinstar/action")
    fun executeDinstarAction(@RequestBody action: Map<String, Any>): ResponseEntity<Any> {
        val type = action["type"] as? String ?: "status"
        val slot = (action["slot"] as? Number)?.toInt() ?: 0
        return when (type.uppercase()) {
            "RESTART_SLOT" -> ResponseEntity.ok(dinstar.restartPort(slot))
            "REBOOT" -> ResponseEntity.ok(dinstar.rebootDevice())
            "SIP_UPDATE" -> {
                val ip = action["sip_ip"] as? String ?: "192.168.1.10"
                ResponseEntity.ok(dinstar.updateSipSettings(ip))
            }
            else -> ResponseEntity.ok(dinstar.getPortsRealtimeStatus())
        }
    }

    @GetMapping("/users/pending")
    fun getPendingUsers() = ResponseEntity.ok(approval.getPendingList())

    @GetMapping("/users/approved")
    fun getApprovedUsers() = ResponseEntity.ok(approval.getApprovedUsers())

    @GetMapping("/users/all")
    fun getAllUsers() = ResponseEntity.ok(approval.getAllUsers())

    @PostMapping("/users/approve")
    fun approveUser(@RequestParam userId: String) = ResponseEntity.ok(approval.approveUser(userId))

    @PostMapping("/users/ban")
    fun banUser(@RequestParam userId: String) = ResponseEntity.ok(approval.banUser(userId))

    @GetMapping("/groups/all")
    fun getGroups() = ResponseEntity.ok(core.getAllGroups())

    @GetMapping("/stories/active")
    fun getStories() = ResponseEntity.ok(core.getActiveStories())

    @PostMapping("/security/kill-switch")
    fun killSwitch(@RequestParam userId: String): ResponseEntity<Any> {
        println("⚠️ RED Master Security: Remote Wipe triggered for $userId")
        return ResponseEntity.ok(mapOf("action" to "WIPE_SIGNAL_SENT", "userId" to userId, "status" to "EXECUTED"))
    }
}
