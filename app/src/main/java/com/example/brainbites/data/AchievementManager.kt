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
        allFacts: List<BiteItem>
    ): List<Achievement> {
        val historyIds = historyItems.map { it.factId }.toSet()
        val uniqueFactsCount = historyIds.size
        
        // Calculate Categories Explored
        val categoriesExplored = allFacts.filter { it.id in historyIds }
            .map { it.category }
            .distinct()
            .size

        // Calculate Timing Achievements
        val calendar = Calendar.getInstance()
        val hasNightOwl = historyItems.any { 
            calendar.timeInMillis = it.timestamp
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            hour >= 22 || hour < 4 // After 10 PM or before 4 AM
        }
        val hasEarlyBird = historyItems.any {
            calendar.timeInMillis = it.timestamp
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            hour >= 5 && hour < 8 // 5 AM to 8 AM
        }

        return listOf(
            Achievement(
                id = "first_step",
                title = "First Step",
                description = "Read your very first psychology fact.",
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                currentProgress = if (uniqueFactsCount > 0) 1 else 0,
                maxProgress = 1,
                status = getStatus(if (uniqueFactsCount > 0) 1 else 0, 1)
            ),
            Achievement(
                id = "scholar",
                title = "The Scholar",
                description = "Read 10 unique psychology facts.",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                currentProgress = uniqueFactsCount,
                maxProgress = 10,
                status = getStatus(uniqueFactsCount, 10)
            ),
            Achievement(
                id = "curator",
                title = "The Curator",
                description = "Save 5 facts to your favorites.",
                icon = Icons.Default.Favorite,
                currentProgress = favoritesCount,
                maxProgress = 5,
                status = getStatus(favoritesCount, 5)
            ),
            Achievement(
                id = "explorer",
                title = "The Explorer",
                description = "Discover facts from 5 different categories.",
                icon = Icons.Default.Explore,
                currentProgress = categoriesExplored,
                maxProgress = 5,
                status = getStatus(categoriesExplored, 5)
            ),
            Achievement(
                id = "night_owl",
                title = "Night Owl",
                description = "Read a fact late at night (after 10 PM).",
                icon = Icons.Default.NightsStay,
                currentProgress = if (hasNightOwl) 1 else 0,
                maxProgress = 1,
                status = getStatus(if (hasNightOwl) 1 else 0, 1)
            ),
            Achievement(
                id = "early_bird",
                title = "Early Bird",
                description = "Start your day with a fact (before 8 AM).",
                icon = Icons.Default.WbSunny,
                currentProgress = if (hasEarlyBird) 1 else 0,
                maxProgress = 1,
                status = getStatus(if (hasEarlyBird) 1 else 0, 1)
            ),
            Achievement(
                id = "thinker",
                title = "The Thinker",
                description = "Read 50 unique facts.",
                icon = Icons.Default.Psychology,
                currentProgress = uniqueFactsCount,
                maxProgress = 50,
                status = getStatus(uniqueFactsCount, 50)
            ),
            Achievement(
                id = "socialite",
                title = "Socialite",
                description = "Share 3 facts with friends.",
                icon = Icons.Default.Share,
                currentProgress = sharesCount,
                maxProgress = 3,
                status = getStatus(sharesCount, 3)
            ),
            Achievement(
                id = "librarian",
                title = "Librarian",
                description = "Build a collection of 20 favorites.",
                icon = Icons.Default.LocalLibrary,
                currentProgress = favoritesCount,
                maxProgress = 20,
                status = getStatus(favoritesCount, 20)
            ),
            Achievement(
                id = "master",
                title = "Master of Mind",
                description = "Read 100 total facts.",
                icon = Icons.Default.AutoAwesome,
                currentProgress = uniqueFactsCount,
                maxProgress = 100,
                status = getStatus(uniqueFactsCount, 100)
            )
        )
    }

    private fun getStatus(current: Int, max: Int): AchievementStatus {
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
