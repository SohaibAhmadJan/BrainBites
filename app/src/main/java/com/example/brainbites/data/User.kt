package com.example.brainbites.data

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val displayName: String = "Knowledge Seeker",
    val email: String = "",
    val photoUrl: String = "",
    val bio: String = "Curious mind exploring the world of psychology.",
    val isPublic: Boolean = false
)

@Serializable
data class UserStats(
    val streakCount: Int = 0,
    val factsReadCount: Int = 0,
    val favoritesCount: Int = 0,
    val sharesCount: Int = 0,
    val lastActiveAt: Long = 0
)

@Serializable
data class UserPreferences(
    val dailyGoal: Int = 5,
    val textScale: Float = 1.0f,
    val hapticsEnabled: Boolean = true,
    val analyticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
)

@Serializable
data class UserAccount(
    val uid: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE" // ACTIVE, DISABLED
)

data class BrainBitesUser(
    val account: UserAccount,
    val profile: UserProfile = UserProfile(),
    val stats: UserStats = UserStats(),
    val preferences: UserPreferences = UserPreferences()
)
