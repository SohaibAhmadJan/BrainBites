package com.example.brainbites.data

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.GENERAL
)

enum class NotificationType {
    NEW_FACT,
    ACHIEVEMENT,
    SYSTEM,
    GENERAL
}
