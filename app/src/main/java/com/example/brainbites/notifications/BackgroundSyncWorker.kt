package com.example.brainbites.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.brainbites.data.NotificationRepository
import android.util.Log

class BackgroundSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("BackgroundSyncWorker", ">>> Background Sync Lifecycle Started")
        
        try {
            // Trigger the repository to pull and show new alerts
            NotificationRepository.manualSync(applicationContext)
            return Result.success()
        } catch (e: Exception) {
            Log.e("BackgroundSyncWorker", "Sync Protocol Failure", e)
            return Result.retry()
        }
    }
}
