package com.example.brainbites.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object AnalyticsRepository {
    private val db = FirebaseFirestore.getInstance()

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
}
