package com.example.brainbites.data

import kotlinx.serialization.Serializable

@Serializable
data class AdminUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: AdminRole = AdminRole.CONTENT_MANAGER,
    val permissions: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class AdminRole {
    SUPER_ADMIN,
    ADMIN,
    CONTENT_MANAGER,
    ANALYST
}

@Serializable
data class AdminActivityLog(
    val id: String = "",
    val adminUid: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val before: String? = null,
    val after: String? = null,
    val reason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
