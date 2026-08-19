package com.example.brainbites.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

object NotificationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())

    fun startListening() {
        val uid = AuthRepository.currentUser.value?.account?.uid ?: return
        
        // Listen to Global Notifications
        db.collection("notifications")
            .whereEqualTo("isGlobal", true)
            .addSnapshotListener { _, _ ->
                syncNotifications(uid)
            }
            
        // Listen to Targeted Notifications
        db.collection("users").document(uid).collection("notifications")
            .addSnapshotListener { _, _ ->
                syncNotifications(uid)
            }
    }

    private fun syncNotifications(uid: String) {
        kotlinx.coroutines.MainScope().launch {
            try {
                val global = db.collection("notifications").whereEqualTo("isGlobal", true).get().await()
                val targeted = db.collection("users").document(uid).collection("notifications").get().await()
                
                val all = (global.documents + targeted.documents).mapNotNull { doc ->
                    try {
                        Notification(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            message = doc.getString("message") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            isRead = doc.getBoolean("isRead") ?: false,
                            type = try { NotificationType.valueOf(doc.getString("type") ?: "GENERAL") } catch(e: Exception) { NotificationType.GENERAL },
                            imageUrl = doc.getString("imageUrl"),
                            deepLinkFactId = doc.getString("deepLinkFactId")
                        )
                    } catch (e: Exception) { null }
                }.sortedByDescending { it.timestamp }
                
                _notifications.value = all
            } catch (e: Exception) { 
                Log.e("NotificationRepository", "Error syncing notifications", e)
            }
        }
    }

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
