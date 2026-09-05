package com.example.brainbites.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import android.content.Context
import com.example.brainbites.notifications.NotificationHelper
import com.example.brainbites.notifications.NotificationWorker
import com.google.firebase.firestore.DocumentChange
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object NotificationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())

    private var sessionStartTime = 0L
    private var isGlobalListening = false
    private var isUserListening = false
    
    // Deduplication Set: Prevents showing the same alert twice in one session
    private val processedIds = mutableSetOf<String>()
    
    // Persistent Read Set: Tracks which notifications the user has viewed
    private val readIds = mutableSetOf<String>()
    private const val PREFS_NAME = "notification_prefs"
    private const val READ_IDS_KEY = "read_notification_ids"
    private const val PROCESSED_IDS_KEY = "processed_notification_ids"

    fun startGlobalListener(context: Context) {
        if (isGlobalListening) return
        isGlobalListening = true
        sessionStartTime = System.currentTimeMillis()
        
        // Load persistent states
        loadReadIds(context)
        loadProcessedIds(context)
        
        Log.d("NotificationRepository", ">>> HEARTBEAT: Starting Global Watcher at $sessionStartTime")
        
        var isInitialSnapshot = true
        
        // 1. Listen to Global Notifications
        db.collection("notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepository", "Global listener error", error)
                    return@addSnapshotListener
                }

                snapshot?.let {
                    Log.d("NotificationRepository", "Global snapshot: ${it.documentChanges.size} items (Initial: $isInitialSnapshot)")
                    it.documentChanges.forEach { change ->
                        if (change.type == DocumentChange.Type.ADDED) {
                            val doc = change.document
                            val isGlobal = doc.getBoolean("isGlobal") ?: false
                            val targetUserId = doc.getString("targetUserId")
                            val currentUid = AuthRepository.currentUser.value?.account?.uid

                            // RECOVERY LOGIC:
                            // We only process incoming alerts (drop-downs) if:
                            // 1. It's NOT the initial dump of history (isInitialSnapshot = false)
                            // 2. OR it has a future scheduled time (even if in history, it needs to be set)
                            // 3. AND it matches the audience criteria
                            val timestamp = doc.getLong("timestamp") ?: 0L
                            val scheduledAt = doc.getLong("scheduledAt") ?: 0L
                            val isFuture = scheduledAt > System.currentTimeMillis()
                            val isNewSinceOpen = timestamp > sessionStartTime

                            if ((!isInitialSnapshot || isFuture) && (isGlobal || (targetUserId != null && targetUserId == currentUid))) {
                                if (isNewSinceOpen || isFuture) {
                                    processIncomingNotification(context, doc)
                                }
                            }
                        }
                    }
                    isInitialSnapshot = false
                    updateLocalList(AuthRepository.currentUser.value?.account?.uid)
                }
            }
    }

    fun startUserListener(context: Context, uid: String) {
        if (isUserListening) return
        isUserListening = true
        Log.d("NotificationRepository", "Starting User Watcher for UID: $uid")
        
        var isInitialSnapshot = true
            
        // 2. Listen to Targeted Notifications
        db.collection("users").document(uid).collection("notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepository", "Targeted listener error", error)
                    return@addSnapshotListener
                }
                snapshot?.let {
                    Log.d("NotificationRepository", "Targeted snapshot: ${it.documentChanges.size} items (Initial: $isInitialSnapshot)")
                    it.documentChanges.forEach { change ->
                        if (change.type == DocumentChange.Type.ADDED) {
                            val doc = change.document
                            val timestamp = doc.getLong("timestamp") ?: 0L
                            val scheduledAt = doc.getLong("scheduledAt") ?: 0L
                            val isFuture = scheduledAt > System.currentTimeMillis()
                            val isNewSinceOpen = timestamp > sessionStartTime

                            if (!isInitialSnapshot || isFuture) {
                                if (isNewSinceOpen || isFuture) {
                                    processIncomingNotification(context, doc)
                                }
                            }
                        }
                    }
                    isInitialSnapshot = false
                    updateLocalList(uid)
                }
            }
    }

    private fun processIncomingNotification(context: Context, doc: com.google.firebase.firestore.DocumentSnapshot) {
        val id = doc.id
        if (processedIds.contains(id)) {
            Log.d("NotificationRepository", "Skipping already processed ID: $id")
            return
        }
        processedIds.add(id)
        saveProcessedIds(context)

        val timestamp = doc.getLong("timestamp") ?: 0L
        val title = doc.getString("title") ?: "BrainBites Update"
        val message = doc.getString("message") ?: "New content available!"
        val factId = doc.getString("deepLinkFactId")
        val imageUrl = doc.getString("imageUrl")
        val scheduledAt = doc.getLong("scheduledAt") ?: 0L

        Log.d("NotificationRepository", "Processing: $title (ts: $timestamp, schedule: $scheduledAt)")

        // 1. If it's a future notification, schedule it locally
        if (scheduledAt > System.currentTimeMillis()) {
            val delay = scheduledAt - System.currentTimeMillis()
            Log.d("NotificationRepository", "Scheduling future notification with delay: ${delay/1000}s")
            
            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(
                    "notificationId" to id,
                    "title" to title,
                    "message" to message,
                    "factId" to factId,
                    "imageUrl" to imageUrl
                ))
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            return
        }

        // 2. If it's immediate, trigger now
        Log.d("NotificationRepository", "Triggering OS notification: $title")
        NotificationHelper.showNotification(
            context = context,
            notificationId = id,
            title = title,
            message = message,
            factId = factId,
            imageUrl = imageUrl
        )
    }

    private fun updateLocalList(uid: String?) {
        kotlinx.coroutines.MainScope().launch {
            try {
                Log.d("NotificationRepository", "Syncing local notification list (UID: $uid)...")
                val globalSnap = db.collection("notifications").get().await()
                
                val targetedDocs = if (uid != null && uid != "anonymous") {
                    db.collection("users").document(uid).collection("notifications").get().await().documents
                } else {
                    emptyList()
                }
                
                val all = (globalSnap.documents + targetedDocs).mapNotNull { doc ->
                    try {
                        val isGlobal = doc.getBoolean("isGlobal") ?: false
                        val targetUserId = doc.getString("targetUserId")
                        
                        // Security check: Only include root notifications that are Global or targeted to ME
                        if (doc.reference.path.startsWith("notifications/")) {
                            if (!isGlobal && (targetUserId == null || targetUserId != uid)) return@mapNotNull null
                        }

                        Notification(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            message = doc.getString("message") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            isRead = readIds.contains(doc.id), // Use persistent local status
                            type = try { NotificationType.valueOf(doc.getString("type") ?: "GENERAL") } catch(e: Exception) { NotificationType.GENERAL },
                            audience = doc.getString("audience"),
                            imageUrl = doc.getString("imageUrl"),
                            deepLinkFactId = doc.getString("deepLinkFactId")
                        )
                    } catch (e: Exception) { null }
                }.distinctBy { it.id }.sortedByDescending { it.timestamp }
                
                _notifications.value = all
            } catch (e: Exception) { 
                Log.e("NotificationRepository", "FATAL: Error updating local list", e)
            }
        }
    }

    private fun loadReadIds(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(READ_IDS_KEY, emptySet()) ?: emptySet()
        readIds.clear()
        readIds.addAll(saved)
    }

    private fun saveReadIds(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(READ_IDS_KEY, readIds).apply()
    }

    private fun loadProcessedIds(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(PROCESSED_IDS_KEY, emptySet()) ?: emptySet()
        processedIds.clear()
        processedIds.addAll(saved)
    }

    private fun saveProcessedIds(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(PROCESSED_IDS_KEY, processedIds).apply()
    }

    /**
     * Manual Sync for Background Worker
     * Pulls the latest 10 notifications and processes any that haven't been shown yet.
     */
    suspend fun manualSync(context: Context) {
        try {
            Log.d("NotificationRepository", "Performing Manual Background Sync...")
            loadProcessedIds(context)
            
            val snapshot = db.collection("notifications")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                val id = doc.id
                val isGlobal = doc.getBoolean("isGlobal") ?: false
                val targetUserId = doc.getString("targetUserId")
                val currentUid = AuthRepository.currentUser.value?.account?.uid

                if (!processedIds.contains(id) && (isGlobal || (targetUserId != null && targetUserId == currentUid))) {
                    processIncomingNotification(context, doc)
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Manual sync failed", e)
        }
    }

    fun getNotifications(): Flow<List<Notification>> = _notifications.asStateFlow()

    fun getUnreadCount(): Flow<Int> = _notifications.map { list -> list.count { !it.isRead } }

    fun markAsRead(context: Context, id: String) {
        readIds.add(id)
        saveReadIds(context)
        
        val currentList = _notifications.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isRead = true)
            _notifications.value = currentList
        }
    }

    fun markAllAsRead(context: Context) {
        _notifications.value.forEach { readIds.add(it.id) }
        saveReadIds(context)
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun clearAll() {
        _notifications.value = emptyList()
    }

    fun addNotification(notification: Notification) {
        _notifications.value = listOf(notification) + _notifications.value
    }
}
