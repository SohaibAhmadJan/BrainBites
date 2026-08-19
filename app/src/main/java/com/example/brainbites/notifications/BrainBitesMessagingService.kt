package com.example.brainbites.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.brainbites.MainActivity
import com.example.brainbites.data.Notification
import com.example.brainbites.data.NotificationRepository
import com.example.brainbites.data.NotificationType
import com.example.brainbites.data.AuthRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.UUID
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BrainBitesMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "BrainBites Update"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Check out what's new!"
        val typeStr = remoteMessage.data["type"] ?: "GENERAL"
        val imageUrl = remoteMessage.data["imageUrl"]
        val deepLinkFactId = remoteMessage.data["deepLinkFactId"]
        
        val type = try {
            NotificationType.valueOf(typeStr)
        } catch (_: Exception) {
            NotificationType.GENERAL
        }

        val notification = Notification(
            id = UUID.randomUUID().toString(),
            title = title,
            message = body,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            type = type,
            imageUrl = imageUrl,
            deepLinkFactId = deepLinkFactId
        )

        // Add to local repository
        NotificationRepository.addNotification(notification)

        // Show system notification
        sendNotification(title, body, imageUrl, deepLinkFactId)
    }

    private fun sendNotification(title: String, messageBody: String, imageUrl: String? = null, factId: String? = null) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        
        if (factId != null) {
            intent.putExtra("factId", factId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "brain_bites_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)


        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "BrainBites Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        MainScope().launch {
            AuthRepository.syncDeviceToken(applicationContext, token)
        }
    }
}
