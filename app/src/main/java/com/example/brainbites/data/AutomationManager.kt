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
            // Listen to User's specific preferences, not global admin settings
            PreferenceManager.isNotificationsEnabled.collect { enabled ->
                if (enabled) {
                    val time = PreferenceManager.dailyNotificationTime.value
                    scheduleDailyPulse(context, time)
                } else {
                    cancelDailyPulse(context)
                }
            }
        }
        
        // Also react if the user changes the time while notifications are already enabled
        scope.launch(Dispatchers.Main) {
            PreferenceManager.dailyNotificationTime.collect { time ->
                if (PreferenceManager.isNotificationsEnabled.value) {
                    scheduleDailyPulse(context, time)
                }
            }
        }
    }

    private fun scheduleDailyPulse(context: Context, timeStr: String) {
        Log.d(TAG, "Scheduling personalized offline automation: $timeStr")
        
        val workManager = WorkManager.getInstance(context)

        // Parse format "hh:mm a" (e.g., "09:00 AM" or "02:30 PM")
        var hour = 9
        var minute = 0
        try {
            val isPm = timeStr.contains("PM", ignoreCase = true)
            val cleanTime = timeStr.replace(" AM", "", ignoreCase = true).replace(" PM", "", ignoreCase = true)
            val parts = cleanTime.split(":")
            if (parts.size == 2) {
                var rawHour = parts[0].toInt()
                minute = parts[1].toInt()
                
                // Convert 12-hour to 24-hour format for Calendar
                if (isPm && rawHour != 12) rawHour += 12
                if (!isPm && rawHour == 12) rawHour = 0
                
                hour = rawHour
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time string: $timeStr", e)
        }

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

        // For local offline user-scheduled tasks, we will rigidly force it to once per day (24 hours).
        val intervalHours = 24L

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
