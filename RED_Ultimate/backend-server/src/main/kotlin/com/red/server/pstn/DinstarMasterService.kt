package com.red.server.pstn

import org.asteriskjava.manager.DefaultManagerConnection
import org.asteriskjava.manager.action.OriginateAction
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class DinstarMasterService {
    
    @Value("\${ASTERISK_IP}")
    private lateinit var asteriskHost: String

    @Value("${ASTERISK_AMI_USER}")
    private lateinit var amiUser: String

    @Value("${ASTERISK_AMI_PASSWORD}")
    private lateinit var amiPassword: String

    private lateinit var managerConnection: DefaultManagerConnection

    @PostConstruct
    fun connectToAsterisk() {
        managerConnection = DefaultManagerConnection(asteriskHost, amiUser, amiPassword)
        try {
            managerConnection.login()
            println("🔴 RED: System B (PSTN) connected to Asterisk Gateway.")
        } catch (e: Exception) {
            println("⚠️ RED: Asterisk Gateway unreachable. Check System B config.")
        }
    }

    /**
     * تنفيذ مكالمة GSM حقيقية عبر DINSTAR
     * Slot selection logic included (Round-robin)
     */
    fun dialThroughDinstar(targetNumber: String, slotIndex: Int = 1): String {
        val action = OriginateAction().apply {
            // توجيه المكالمة إلى الـ SIM المحددة في Dinstar
            channel = "PJSIP/$targetNumber@dinstar-slot-$slotIndex"
            context = "from-internal"
            exten = "s"
            priority = 1
            callerId = "RED SOVEREIGN"
        }
        val response = managerConnection.sendAction(action)
        return response.actionId
    }
}
