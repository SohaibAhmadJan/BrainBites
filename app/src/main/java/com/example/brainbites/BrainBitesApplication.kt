package com.example.brainbites

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.util.DebugLogger
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.brainbites.notifications.BackgroundSyncWorker
import java.util.concurrent.TimeUnit

class BrainBitesApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        scheduleBackgroundSync()
    }

    private fun scheduleBackgroundSync() {
        val syncRequest = PeriodicWorkRequestBuilder<BackgroundSyncWorker>(
            15, TimeUnit.MINUTES, // Pull every 15 minutes (Android OS minimum)
            5, TimeUnit.MINUTES   // Flex interval
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BackgroundSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .logger(DebugLogger()) // Add logger to see why images fail
            .build()
    }
}
