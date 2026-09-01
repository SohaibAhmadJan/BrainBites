package com.example.brainbites.data

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.brainbites.notifications.DailyFactWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

object AutomationManager {
    private const val TAG = "AutomationManager"
    private const val WORK_NAME = "DAILY_FACT_WORKER"

    fun initialize(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
            SettingsRepository.settings.collect { settings ->
                if (settings.automationEnabled) {
                    scheduleDailyPulse(context, settings)
                } else {
                    cancelDailyPulse(context)
                }
            }
        }
    }

    private fun scheduleDailyPulse(context: Context, settings: AppSettings) {
        Log.d(TAG, "Scheduling automation: ${settings.dailyNotificationTime} (${settings.notificationFrequency})")
        
        val workManager = WorkManager.getInstance(context)

        // Parse time (Expected format "HH:mm")
        val timeParts = settings.dailyNotificationTime.split(":")
        val hour = timeParts.getOrNull(0)?.toInt() ?: 9
        val minute = timeParts.getOrNull(1)?.toInt() ?: 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val now = System.currentTimeMillis()
        val gracePeriod = 2 * 60 * 1000L // 2-minute grace window for "Use Current Time" latency

        var initialDelay = calendar.timeInMillis - now

        if (initialDelay < -gracePeriod) {
            // Time passed more than 2 minutes ago -> schedule for the next cycle (tomorrow)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            initialDelay = calendar.timeInMillis - now
        } else if (initialDelay < 0) {
            // Time passed within the last 2 minutes -> trigger IMMEDIATELY
            initialDelay = 0
        }

        val intervalHours = when (settings.notificationFrequency) {
            "DAILY" -> 24L
            "2_TIMES_DAILY" -> 12L
            "3_TIMES_DAILY" -> 8L
            "4_TIMES_DAILY" -> 6L
            "6_TIMES_DAILY" -> 4L
            "8_TIMES_DAILY" -> 3L
            "EVERY_2_DAYS" -> 48L
            "WEEKLY" -> 168L
            else -> 24L
        }

        val workRequest = PeriodicWorkRequestBuilder<DailyFactWorker>(
            intervalHours, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .addTag(WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // Update if changed
            workRequest
        )
        
        Log.d(TAG, "Sync Scheduled. Initial delay: ${initialDelay / 1000 / 60} minutes")
    }

    private fun cancelDailyPulse(context: Context) {
        Log.d(TAG, "Automation disabled. Cancelling all pulses.")
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
