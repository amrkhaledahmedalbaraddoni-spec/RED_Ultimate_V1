package com.red.server.groups

import com.red.server.auth.repository.UserAccountRepository
import com.red.server.social.UuidV7
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class GroupService(private val mongo: MongoTemplate, private val users: UserAccountRepository) {
    fun create(ownerId: UUID, request: CreateGroupRequest): GroupResponse {
        val owner = users.findById(ownerId).orElseThrow { NoSuchElementException("User not found") }
        val name = request.name.trim(); require(name.length in 2..100) { "Group name must be 2-100 characters" }
        val description = request.description?.trim()?.takeIf(String::isNotEmpty); require(description == null || description.length <= 500)
        val group = mongo.save(GroupDocument(UuidV7.next(), name, description, owner.redId))
        mongo.save(GroupMember("${group.id}:${owner.id}", group.id, owner.id.toString(), owner.redId, owner.username, GroupRole.OWNER))
        return response(group)
    }

    fun list(userId: UUID): List<GroupResponse> {
        val ids = mongo.find(Query(Criteria.where("userId").`is`(userId.toString())), GroupMember::class.java).map(GroupMember::groupId)
        if (ids.isEmpty()) return emptyList()
        return mongo.find(Query(Criteria.where("id").`in`(ids)).with(Sort.by(Sort.Direction.DESC, "updatedAt")), GroupDocument::class.java).map(::response)
    }

    fun details(userId: UUID, groupId: String): GroupResponse {
        membership(groupId, userId)
        return response(group(groupId))
    }

    fun add(actorId: UUID, groupId: String, request: AddGroupMemberRequest): GroupResponse {
        requireManager(groupId, actorId)
        require(request.role != GroupRole.OWNER) { "Ownership transfer requires a dedicated operation" }
        val target = users.findByRedId(request.redId) ?: throw NoSuchElementException("RED identity not found")
        val id = "$groupId:${target.id}"
        require(!mongo.exists(Query(Criteria.where("id").`is`(id)), GroupMember::class.java)) { "User is already a member" }
        mongo.save(GroupMember(id, groupId, target.id.toString(), target.redId, target.username, request.role))
        touch(groupId)
        return response(group(groupId))
    }

    fun role(actorId: UUID, groupId: String, targetUserId: UUID, request: UpdateGroupRoleRequest): GroupResponse {
        val actor = membership(groupId, actorId); require(actor.role == GroupRole.OWNER) { "Only owner can change roles" }
        require(request.role != GroupRole.OWNER) { "Ownership transfer is not supported yet" }
        val target = membership(groupId, targetUserId); require(target.role != GroupRole.OWNER)
        target.role = request.role; mongo.save(target); touch(groupId)
        return response(group(groupId))
    }

    fun remove(actorId: UUID, groupId: String, targetUserId: UUID): GroupResponse {
        val actor = membership(groupId, actorId); val target = membership(groupId, targetUserId)
        require(target.role != GroupRole.OWNER) { "Owner cannot be removed" }
        require(actor.role == GroupRole.OWNER || (actor.role == GroupRole.ADMIN && target.role == GroupRole.MEMBER)) { "Insufficient group permission" }
        mongo.remove(Query(Criteria.where("id").`is`(target.id)), GroupMember::class.java); touch(groupId)
        return response(group(groupId))
    }

    fun leave(userId: UUID, groupId: String) {
        val member = membership(groupId, userId); require(member.role != GroupRole.OWNER) { "Owner must transfer or delete the group" }
        mongo.remove(Query(Criteria.where("id").`is`(member.id)), GroupMember::class.java); touch(groupId)
    }

    fun count(): Long = mongo.count(Query(), GroupDocument::class.java)

    private fun group(id: String) = mongo.findById(id, GroupDocument::class.java) ?: throw NoSuchElementException("Group not found")
    private fun membership(groupId: String, userId: UUID) = mongo.findOne(Query(Criteria.where("id").`is`("$groupId:$userId")), GroupMember::class.java)
        ?: throw NoSuchElementException("Group membership not found")
    private fun requireManager(groupId: String, userId: UUID) = membership(groupId, userId).also { require(it.role == GroupRole.OWNER || it.role == GroupRole.ADMIN) }
    private fun touch(groupId: String) { group(groupId).also { it.updatedAt = Instant.now(); mongo.save(it) } }
    private fun response(group: GroupDocument) = GroupResponse(group.id, group.name, group.description, group.ownerRedId, group.createdAt,
        mongo.find(Query(Criteria.where("groupId").`is`(group.id)).with(Sort.by(Sort.Direction.ASC, "joinedAt")), GroupMember::class.java))
}
