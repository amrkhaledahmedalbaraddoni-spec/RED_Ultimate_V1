package com.red.server.pstn

import jakarta.annotation.PreDestroy
import org.asteriskjava.manager.DefaultManagerConnection
import org.asteriskjava.manager.action.OriginateAction
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class PstnManager(
    @Value("\${ASTERISK_AMI_HOST:red-pstn-gateway}") private val amiHost: String,
    @Value("\${ASTERISK_AMI_USER:red_admin}") private val amiUser: String,
    @Value("\${ASTERISK_AMI_PASSWORD:}") private val amiPassword: String
) {
    private val connectionDelegate = lazy {
        require(amiPassword.isNotBlank()) { "ASTERISK_AMI_PASSWORD must be configured" }
        DefaultManagerConnection(amiHost, amiUser, amiPassword).also { it.login() }
    }
    private val connection by connectionDelegate

    fun dialGsm(phoneNumber: String): String {
        require(phoneNumber.matches(Regex("^\\+?[0-9]{6,15}$"))) { "Invalid phone number" }
        val action = OriginateAction().apply {
            channel = "PJSIP/$phoneNumber@dinstar-gateway"
            context = "from-internal"
            exten = "s"
            priority = 1
            callerId = "RED SOVEREIGN"
        }
        val response = connection.sendAction(action)
        return response.actionId ?: "accepted"
    }

    @PreDestroy
    fun close() {
        if (connectionDelegate.isInitialized()) runCatching { connection.logoff() }
    }
}
