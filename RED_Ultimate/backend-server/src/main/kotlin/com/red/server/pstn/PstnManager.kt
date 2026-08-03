package com.red.server.pstn

import org.asteriskjava.manager.DefaultManagerConnection
import org.asteriskjava.manager.action.OriginateAction
import org.springframework.stereotype.Service

@Service
class PstnManager {
    @Value("${ASTERISK_AMI_USER}")
    private lateinit var amiUser: String

    @Value("${ASTERISK_AMI_PASSWORD}")
    private lateinit var amiPassword: String

    private val asterisk by lazy { 
        DefaultManagerConnection("red-pstn-gateway", amiUser, amiPassword) 
    }

    init {
        try {
            asterisk.login()
            println("🔴 RED PSTN: Connected to Asterisk Gateway.")
        } catch (e: Exception) {
            println("⚠️ RED PSTN: Gateway connection failed.")
        }
    }

    fun dialGsm(phoneNumber: String): String {
        val action = OriginateAction().apply {
            channel = "PJSIP/$phoneNumber@dumin-trunk"
            context = "from-internal"
            exten = "s"
            priority = 1
            callerId = "RED SOVEREIGN"
        }
        val response = asterisk.sendAction(action)
        return response.actionId
    }
}
