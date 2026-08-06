package com.red.sovereign.groups

import kotlinx.serialization.Serializable

@Serializable data class GroupMember(val id: String, val groupId: String, val userId: String, val redId: String, val username: String, val role: String, val joinedAt: String)
@Serializable data class Group(val id: String, val name: String, val description: String? = null, val ownerRedId: String, val createdAt: String, val members: List<GroupMember> = emptyList())
@Serializable data class CreateGroupRequest(val name: String, val description: String? = null)
@Serializable data class AddGroupMemberRequest(val redId: String, val role: String = "MEMBER")
@Serializable data class UpdateGroupRoleRequest(val role: String)
