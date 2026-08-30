package com.example.brainbites.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.brainbites.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "brain_bites_notifications"
    private const val CHANNEL_NAME = "BrainBites Notifications"

    fun showNotification(
        context: Context,
        notificationId: String,
        title: String,
        message: String,
        factId: String? = null,
        imageUrl: String? = null
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "General notifications for BrainBites insights and updates"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Setup Intent for deep linking
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (factId != null) {
                putExtra("factId", factId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(), // Unique ID per notification to prevent intent overlap
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Use MAX for wireless reliability
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Use the hash of the Firestore ID to ensure unique slots in the system tray
        val osNotificationId = notificationId.hashCode()
        notificationManager.notify(osNotificationId, builder.build())
    }
}
