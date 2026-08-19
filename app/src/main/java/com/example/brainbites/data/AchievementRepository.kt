package com.example.brainbites.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val maxProgress: Int,
    val iconName: String,
    val requirementType: String,
    val isActive: Boolean = true
)

object AchievementRepository {
    private val db = FirebaseFirestore.getInstance()
    private val _definitions = MutableStateFlow<List<AchievementDefinition>>(emptyList())
    val definitions = _definitions.asStateFlow()

    private val _userAchievements = MutableStateFlow<Map<String, Int>>(emptyMap())
    val userAchievements = _userAchievements.asStateFlow()

    suspend fun fetchDefinitions() {
        try {
            val snapshot = db.collection("achievements").whereEqualTo("isActive", true).get().await()
            _definitions.value = snapshot.documents.mapNotNull { doc ->
                try {
                    AchievementDefinition(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        maxProgress = doc.getLong("maxProgress")?.toInt() ?: 1,
                        iconName = doc.getString("iconName") ?: "Star",
                        requirementType = doc.getString("requirementType") ?: "READ_COUNT"
                    )
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            Log.e("AchievementRepository", "Error fetching definitions", e)
        }
    }

    suspend fun syncUserAchievements(uid: String) {
        try {
            val snapshot = db.collection("users").document(uid).collection("achievements").get().await()
            _userAchievements.value = snapshot.documents.associate { doc ->
                doc.id to (doc.getLong("progress")?.toInt() ?: 0)
            }
        } catch (e: Exception) {
            Log.e("AchievementRepository", "Error syncing user achievements", e)
        }
    }

    suspend fun updateProgress(uid: String, achievementId: String, progress: Int) {
        try {
            db.collection("users").document(uid).collection("achievements").document(achievementId)
                .set(mapOf("progress" to progress, "updatedAt" to System.currentTimeMillis()))
                .await()
            val current = _userAchievements.value.toMutableMap()
            current[achievementId] = progress
            _userAchievements.value = current
        } catch (e: Exception) {
            Log.e("AchievementRepository", "Error updating achievement progress", e)
        }
    }
}
