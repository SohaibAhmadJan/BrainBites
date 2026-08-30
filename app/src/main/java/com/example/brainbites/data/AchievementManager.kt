package com.example.brainbites.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import java.util.Calendar

object AchievementManager {
    fun calculateAchievements(
        historyItems: List<HistoryItem>,
        favoritesCount: Int,
        sharesCount: Int,
        allFacts: List<BiteItem>,
        definitions: List<AchievementDefinition>
    ): List<Achievement> {
        val historyIds = historyItems.map { it.factId }.toSet()
        val uniqueFactsCount = historyIds.size
        
        // Calculate Categories Explored
        val categoriesExplored = allFacts.filter { it.id in historyIds }
            .map { it.category }
            .distinct()
            .size

        // Calculate Timing
        val calendar = Calendar.getInstance()
        val hasNightOwl = historyItems.any { 
            calendar.timeInMillis = it.timestamp
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            hour >= 22 || hour < 4
        }
        val hasEarlyBird = historyItems.any {
            calendar.timeInMillis = it.timestamp
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            hour >= 5 && hour < 8
        }

        return definitions.map { def ->
            val progress = when (def.requirementType) {
                "READ_COUNT" -> uniqueFactsCount
                "FAVORITE_COUNT" -> favoritesCount
                "SHARE_COUNT" -> sharesCount
                "CATEGORY_COUNT" -> categoriesExplored
                "STREAK_DAYS" -> PreferenceManager.streakCount.value
                "NIGHT_OWL" -> if (hasNightOwl) 1 else 0
                "EARLY_BIRD" -> if (hasEarlyBird) 1 else 0
                else -> 0
            }

            Achievement(
                id = def.id,
                title = def.title,
                description = def.description,
                currentProgress = progress,
                maxProgress = def.maxProgress,
                status = getStatus(progress, def.maxProgress),
                iconName = def.iconName,
                icon = Icons.Default.EmojiEvents 
            )
        }
    }

    private fun getStatus(current: Int, max: Int): AchievementStatus {
        if (max <= 0) return AchievementStatus.COMPLETED
        return when {
            current >= max -> AchievementStatus.COMPLETED
            current > 0 -> AchievementStatus.IN_PROGRESS
            else -> AchievementStatus.LOCKED
        }
    }
    
    fun getAchievementInsight(id: String): String {
        return when (id) {
            "scholar" -> "Knowledge is a treasure, but practice is the key to it."
            "curator" -> "You are building your own library of wisdom."
            "philosopher" -> "The unexamined life is not worth living."
            "consistent" -> "We are what we repeatedly do. Excellence, then, is not an act, but a habit."
            else -> "Keep learning, keep growing."
        }
    }
}
