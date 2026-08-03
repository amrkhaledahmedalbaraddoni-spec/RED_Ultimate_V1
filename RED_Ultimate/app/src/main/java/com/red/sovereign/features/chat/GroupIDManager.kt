package com.red.sovereign.features.chat

import com.red.sovereign.core.auth.IdentityManager
import com.red.sovereign.core.network.RedWebSocketClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupIDManager @Inject constructor(
    private val identityManager: IdentityManager,
    private val webSocketClient: RedWebSocketClient
) {
    fun inviteMemberByHandle(groupId: String, handle: String) {
        println("🔴 RED: Inviting $handle to Group $groupId via ${identityManager.getUserHandle()}")
        webSocketClient.send("""{"type":"group_invite","groupId":"$groupId","handle":"$handle","inviter":"${identityManager.getRedId()}"}""".toByteArray())
    }

    fun getMySovereignID(): String {
        return identityManager.getUserHandle().ifEmpty { "PENDING_APPROVAL" }
    }

    fun createGroup(name: String, members: List<String>): String {
        val groupId = "group-${System.currentTimeMillis()}"
        webSocketClient.send("""{"type":"group_create","groupId":"$groupId","name":"$name","members":${members}}""".toByteArray())
        return groupId
    }
}
