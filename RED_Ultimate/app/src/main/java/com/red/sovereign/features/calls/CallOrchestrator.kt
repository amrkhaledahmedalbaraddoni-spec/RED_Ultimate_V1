package com.red.sovereign.features.calls

import com.red.sovereign.features.pstn.PstnViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallOrchestrator @Inject constructor(
    private val voipMaster: RedVoipMaster,
    private val pstnViewModel: PstnViewModel
) {
    sealed class CallType {
        object VoipAudio : CallType()
        object VoipVideo : CallType()
        data class Pstn(val slot: Int = 0) : CallType()
        data class Conference(val roomId: String) : CallType()
        data class LiveBroadcast(val streamId: String) : CallType()
    }

    fun initiateCall(target: String, isGsm: Boolean) {
        if (isGsm) {
            pstnViewModel.dialPstn(target) // System B
        } else {
            voipMaster.startSecureCall(target, videoEnabled = false) // System A Audio
        }
    }

    fun initiateCall(target: String, type: CallType) {
        when (type) {
            is CallType.VoipAudio -> voipMaster.startSecureCall(target, videoEnabled = false)
            is CallType.VoipVideo -> voipMaster.startSecureCall(target, videoEnabled = true)
            is CallType.Pstn -> pstnViewModel.dialPstn(target, type.slot)
            is CallType.Conference -> startConference(type.roomId)
            is CallType.LiveBroadcast -> startLiveStream(type.streamId)
        }
    }

    fun startConference(roomId: String) {
        voipMaster.startSecureCall(roomId, videoEnabled = true)
        println("🔴 RED Conference $roomId started via SFU")
    }

    fun startLiveStream(streamId: String) {
        voipMaster.startSecureCall(streamId, videoEnabled = true)
        println("🔴 RED Live Broadcast $streamId started")
    }

    fun endAllCalls() {
        voipMaster.endCall()
        pstnViewModel.endGsmCall()
    }
}
