package com.example.brainbites.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.brainbites.data.BiteRepository
import kotlinx.coroutines.flow.first

class DailyFactWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("DailyFactWorker", "Executing automated daily fact pulse...")
        
        try {
            // 1. Ensure we have the latest data
            BiteRepository.refreshData(applicationContext, forceRemote = true)
            
            // 2. Get the current fact of the day
            val allFacts = BiteRepository.getAllFacts(applicationContext).first()
            val dailyFact = BiteRepository.getFactOfTheDay(allFacts)
            
            if (dailyFact != null) {
                Log.d("DailyFactWorker", "Dispatched Daily Fact: ${dailyFact.id}")
                
                NotificationHelper.showNotification(
                    context = applicationContext,
                    notificationId = "daily_fact_${System.currentTimeMillis()}",
                    title = "Your Daily Insight 🧠",
                    message = dailyFact.fact,
                    factId = dailyFact.id,
                    imageUrl = dailyFact.imageUrl
                )
                return Result.success()
            } else {
                Log.w("DailyFactWorker", "No facts found for automated dispatch.")
                return Result.failure()
            }
        } catch (e: Exception) {
            Log.e("DailyFactWorker", "Automated pulse failed", e)
            return Result.retry()
        }
    }
}
