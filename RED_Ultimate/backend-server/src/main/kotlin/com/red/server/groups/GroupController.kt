package com.red.server.groups

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/groups")
class GroupController(private val groups: GroupService) {
    @GetMapping fun list(auth: Authentication) = groups.list(UUID.fromString(auth.name))
    @PostMapping fun create(@RequestBody request: CreateGroupRequest, auth: Authentication) = groups.create(UUID.fromString(auth.name), request)
    @GetMapping("/{id}") fun details(@PathVariable id: String, auth: Authentication) = groups.details(UUID.fromString(auth.name), id)
    @PostMapping("/{id}/members") fun add(@PathVariable id: String, @RequestBody request: AddGroupMemberRequest, auth: Authentication) = groups.add(UUID.fromString(auth.name), id, request)
    @PatchMapping("/{id}/members/{userId}") fun role(@PathVariable id: String, @PathVariable userId: UUID, @RequestBody request: UpdateGroupRoleRequest, auth: Authentication) = groups.role(UUID.fromString(auth.name), id, userId, request)
    @DeleteMapping("/{id}/members/{userId}") fun remove(@PathVariable id: String, @PathVariable userId: UUID, auth: Authentication) = groups.remove(UUID.fromString(auth.name), id, userId)
    @DeleteMapping("/{id}/membership") fun leave(@PathVariable id: String, auth: Authentication) = groups.leave(UUID.fromString(auth.name), id)
}
