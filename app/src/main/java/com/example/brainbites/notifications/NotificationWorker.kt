package com.example.brainbites.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val notificationId = inputData.getString("notificationId") ?: System.currentTimeMillis().toString()
        val title = inputData.getString("title") ?: "BrainBites Update"
        val message = inputData.getString("message") ?: "New content available!"
        val factId = inputData.getString("factId")
        val imageUrl = inputData.getString("imageUrl")

        NotificationHelper.showNotification(
            context = applicationContext,
            notificationId = notificationId,
            title = title,
            message = message,
            factId = factId,
            imageUrl = imageUrl
        )

        return Result.success()
    }
}
