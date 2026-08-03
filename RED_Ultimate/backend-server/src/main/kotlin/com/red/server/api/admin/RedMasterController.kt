package com.red.server.api.admin

import com.red.server.services.*
import com.red.server.infrastructure.dinstar.DinstarMasterClient
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/master/v1")
class RedMasterController(
    private val statsService: MasterStatsService,
    private val approvalService: RedApprovalService,
    private val dinstarClient: DinstarMasterClient,
    private val securityService: RedSecurityService
) {
    // 1. تبويب الإحصائيات (System Overview)
    @GetMapping("/stats/realtime")
    fun getGlobalStats() = ResponseEntity.ok(statsService.getLiveMetrics())

    // 2. تبويب الصلاحيات (Authority/Users)
    @GetMapping("/auth/pending")
    fun listPending() = ResponseEntity.ok(approvalService.getPendingList())

    @PostMapping("/auth/action")
    fun handleUserAction(@RequestBody req: Map<String, String>) = 
        ResponseEntity.ok(approvalService.processAction(req["userId"]!!, req["action"]!!))

    // 3. تبويب الهاردوير (Dinstar/PSTN)
    @GetMapping("/hardware/dinstar/slots")
    fun getSlots() = ResponseEntity.ok(dinstarClient.getPortsRealtimeStatus())

    // 4. تبويب الأمن (Kill Switch/Audit)
    @PostMapping("/security/wipe")
    fun initiateWipe(@RequestParam userId: String) = 
        ResponseEntity.ok(securityService.triggerKillSwitch(userId))

    // 5. تبويب الوسائط (Media/SFU)
    @GetMapping("/media/active-calls")
    fun getActiveCalls() = ResponseEntity.ok(statsService.getVoipMetrics())
}
