package com.red.server.pstn

import org.asteriskjava.manager.AbstractManagerEventListener
import org.asteriskjava.manager.event.NewStateEvent
import org.springframework.stereotype.Component

@Component
class DinstarEventListener : AbstractManagerEventListener() {
    
    override fun handleEvent(event: org.asteriskjava.manager.event.ManagerEvent) {
        if (event is NewStateEvent) {
            val state = event.channelStateDesc
            val number = event.channel.split("/").last().split("@").first()
            
            println("🔴 RED Hardware: Line $number is now in state: $state")
            
            // إبلاغ الأندرويد عبر WebSocket بالحالة الحية (Ringing, Up, Busy)
            // signalingService.broadcastCallStatus(number, state)
        }
    }
}
