package com.example.brainbites.data

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.GENERAL,
    val audience: String? = null, // Added for segment labeling
    val imageUrl: String? = null,
    val deepLinkFactId: String? = null,
    val scheduledAt: Long? = null // Added for scheduling
)

enum class NotificationType {
    NEW_FACT,
    ACHIEVEMENT,
    SYSTEM,
    GENERAL
}
