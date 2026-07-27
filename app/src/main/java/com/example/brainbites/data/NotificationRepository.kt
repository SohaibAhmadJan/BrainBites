package com.example.brainbites.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

object NotificationRepository {
    private val _notifications = MutableStateFlow<List<Notification>>(
        listOf(
            Notification(
                id = "1",
                title = "Welcome to BrainBites!",
                message = "Start your journey by reading your first psychology fact today.",
                timestamp = System.currentTimeMillis() - 3600000,
                type = NotificationType.SYSTEM
            ),
            Notification(
                id = "2",
                title = "New Achievement Unlocked!",
                message = "Congratulations! You've unlocked the 'First Quote Read' milestone.",
                timestamp = System.currentTimeMillis() - 86400000,
                type = NotificationType.ACHIEVEMENT
            ),
            Notification(
                id = "3",
                title = "New Fact Available",
                message = "Explore the 'Human Behavior' category for the latest insights.",
                timestamp = System.currentTimeMillis() - 172800000,
                type = NotificationType.NEW_FACT
            )
        )
    )

    fun getNotifications(): Flow<List<Notification>> = _notifications.asStateFlow()

    fun getUnreadCount(): Flow<Int> = _notifications.map { list -> list.count { !it.isRead } }

    fun markAsRead(id: String) {
        val currentList = _notifications.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isRead = true)
            _notifications.value = currentList
        }
    }

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun clearAll() {
        _notifications.value = emptyList()
    }

    fun addNotification(notification: Notification) {
        _notifications.value = listOf(notification) + _notifications.value
    }
}
