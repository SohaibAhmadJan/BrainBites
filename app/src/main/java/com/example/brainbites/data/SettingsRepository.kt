package com.example.brainbites.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SettingsRepository {
    private val _settings = MutableStateFlow(AppSettings())
    val settings = _settings.asStateFlow()

    fun startListening() {
        val db = FirebaseFirestore.getInstance()
        db.collection("app_settings").document("global_config")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("SettingsRepository", "Listen failed", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val flags = snapshot.get("featureFlags") as? Map<*, *>
                        val newSettings = AppSettings(
                            maintenanceMode = snapshot.getBoolean("maintenanceMode") ?: false,
                            maintenanceMessage = snapshot.getString("maintenanceMessage") ?: "",
                            latestVersion = snapshot.getString("latestVersion") ?: "3.4.8.7",
                            minVersion = snapshot.getString("minVersion") ?: "1.0.0",
                            supportEmail = snapshot.getString("supportEmail") ?: "support@brainbites.com",
                            quizzesEnabled = flags?.get("quizzesEnabled") as? Boolean ?: true,
                            achievementsEnabled = flags?.get("achievementsEnabled") as? Boolean ?: true,
                            dailyFactEnabled = flags?.get("dailyFactEnabled") as? Boolean ?: true,
                            featuredFactId = snapshot.getString("featuredFactId") ?: "1",
                            dailyTipTitle = snapshot.getString("dailyTipTitle") ?: "The 2-Minute Rule",
                            dailyTipMessage = snapshot.getString("dailyTipMessage") ?: "If a task takes less than 2 minutes, do it now.",
                            homeSectionsOrder = (snapshot.get("homeSectionsOrder") as? List<*>)?.filterIsInstance<String>() ?: listOf("HERO", "CATEGORIES", "QUICK_ACTIONS", "MOOD", "RECENT", "DISCOVER", "ACHIEVEMENTS", "TIP", "TRENDING")
                        )
                        _settings.value = newSettings
                        Log.d("SettingsRepository", "Remote config updated: $newSettings")
                    } catch (ex: Exception) {
                        Log.e("SettingsRepository", "Error mapping remote config", ex)
                    }
                }
            }
    }
}
