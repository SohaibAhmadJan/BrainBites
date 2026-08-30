package com.example.brainbites.ui.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbites.data.Notification
import com.example.brainbites.data.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {
    val notifications: StateFlow<List<Notification>> = NotificationRepository.getNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun markAsRead(id: String) {
        NotificationRepository.markAsRead(getApplication(), id)
    }

    fun markAllAsRead() {
        NotificationRepository.markAllAsRead(getApplication())
    }

    fun clearAll() {
        NotificationRepository.clearAll()
    }
}
