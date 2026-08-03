package com.red.core.entities

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id val id: String = UUID.randomUUID().toString(),
    @Column(unique = true) val email: String,
    val passwordHash: String,
    val fullName: String,
    
    @Enumerated(EnumType.STRING)
    var status: UserStatus = UserStatus.PENDING, // PENDING, APPROVED, BANNED
    
    var role: String = "USER", // USER, ADMIN
    
    val createdAt: Long = System.currentTimeMillis(),
    
    // إعدادات Dumin الخاصة بالمدير
    var assignedDuminIp: String? = null
)

enum class UserStatus { PENDING, APPROVED, REJECTED, BANNED }
