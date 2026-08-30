package com.example.brainbites.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object AnalyticsRepository {
    private val db get() = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    fun logEvent(name: String, params: Map<String, Any> = emptyMap()) {
        val uid = AuthRepository.currentUser.value?.account?.uid ?: "anonymous"
        val event = mapOf(
            "name" to name,
            "params" to params,
            "uid" to uid,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        
        db.collection("analytics_events").add(event)
            .addOnFailureListener { e -> Log.e("AnalyticsRepository", "Failed to log event $name", e) }
    }

    fun logFactView(factId: String) {
        logEvent("read_fact", mapOf("item_id" to factId))
    }

    fun logCategoryView(categoryId: String) {
        logEvent("category_view", mapOf("category_id" to categoryId))
    }

    fun logShare(factId: String) {
        logEvent("fact_share", mapOf("item_id" to factId))
    }

    fun logSearch(query: String, resultCount: Int) {
        logEvent("content_search", mapOf(
            "query" to query.lowercase(),
            "results" to resultCount
        ))
    }

    fun logAchievement(achievementId: String) {
        logEvent("achievement_unlocked", mapOf("achievement_id" to achievementId))
    }

    fun logAppOpen() {
        logEvent("app_open")
    }

    fun logAppInstall(context: android.content.Context) {
        val prefs = context.getSharedPreferences("brain_bites_device", android.content.Context.MODE_PRIVATE)
        val deviceId = prefs.getString("device_id", "unknown_device")
        logEvent("app_install", mapOf("device_id" to (deviceId ?: "unknown_device")))
    }

    fun initializeInstallation(context: android.content.Context) {
        val prefs = context.getSharedPreferences("brain_bites_device", android.content.Context.MODE_PRIVATE)
        var deviceId = prefs.getString("device_id", null)
        
        if (deviceId == null) {
            deviceId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString("device_id", deviceId).apply()
            Log.d("BRAIN_BITES", "INIT: Generated New ID: $deviceId")
        } else {
            Log.d("BRAIN_BITES", "INIT: Using Existing ID: $deviceId")
        }

        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val appVersion = packageInfo.versionName

        val installationData = mapOf(
            "deviceId" to deviceId,
            "platform" to "android",
            "appVersion" to appVersion,
            "lastSeenAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        Log.d("BRAIN_BITES", "INIT: Attempting Firestore Write for $deviceId")
        db.collection("installations").document(deviceId!!)
            .set(installationData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { 
                Log.d("BRAIN_BITES", "INIT: SUCCESS! Installation registered.") 
            }
            .addOnFailureListener { e -> 
                Log.e("BRAIN_BITES", "INIT: FAILED! Firestore error: ${e.message}", e) 
            }
    }
}
