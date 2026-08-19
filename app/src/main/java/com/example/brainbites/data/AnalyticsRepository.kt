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
        logEvent("fact_view", mapOf("factId" to factId))
    }

    fun logQuizAttempt(factId: String, isCorrect: Boolean) {
        logEvent("quiz_attempt", mapOf("factId" to factId, "isCorrect" to isCorrect))
    }

    fun logShare(factId: String) {
        logEvent("fact_share", mapOf("factId" to factId))
    }
}
