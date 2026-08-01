package com.example.brainbites.ui.profile

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbites.data.Achievement
import com.example.brainbites.data.AchievementManager
import com.example.brainbites.data.AchievementStatus
import com.example.brainbites.data.BiteItem
import com.example.brainbites.data.BiteRepository
import com.example.brainbites.data.CollectionSet
import com.example.brainbites.data.HistoryItem
import com.example.brainbites.data.PreferenceManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CollectionProgress(val collection: CollectionSet, val progress: Float)

data class UserStats(
    val factsRead: Int = 0,
    val favoritesCount: Int = 0,
    val achievementsUnlocked: Int = 0,
    val level: Int = 1,
    val rankTitle: String = "Beginner",
    val streak: Int = 0,
    val collectionProgress: List<CollectionProgress> = emptyList()
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _stats = MutableStateFlow(UserStats())
    val stats = _stats.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements = _achievements.asStateFlow()

    val userName = PreferenceManager.userName
    val userImage = PreferenceManager.userImage
    val userBio = PreferenceManager.userBio
    val userId = PreferenceManager.userId
    val isPublicProfile = PreferenceManager.isPublicProfile
    val isAnalyticsEnabled = PreferenceManager.isAnalyticsEnabled

    init {
        loadStats()
        loadAchievements()
    }

    private fun loadStats() {
        viewModelScope.launch {
            combine(
                BiteRepository.getHistoryItems(),
                BiteRepository.getFavoriteFacts(getApplication()),
                BiteRepository.getSharesCount(),
                BiteRepository.getAllFacts(getApplication()),
                BiteRepository.getAllCollections(),
                PreferenceManager.streakCount
            ) { args: Array<Any> ->
                val history = args[0] as List<com.example.brainbites.data.HistoryItem>
                val favorites = args[1] as List<BiteItem>
                val shares = args[2] as Int
                val allFacts = args[3] as List<BiteItem>
                val collections = args[4] as List<CollectionSet>
                val streak = args[5] as Int

                val currentAchievements = AchievementManager.calculateAchievements(
                    historyItems = history,
                    favoritesCount = favorites.size,
                    sharesCount = shares,
                    allFacts = allFacts
                )
                
                val factsRead = history.size
                val (level, title) = calculateLevelAndRank(factsRead)

                val readIds = history.map { it.factId }.toSet()
                val activeCollections = collections.map { col ->
                    val colIds = col.factIds.toSet()
                    val progress = if (colIds.isEmpty()) 0f else colIds.intersect(readIds).size.toFloat() / colIds.size.toFloat()
                    CollectionProgress(col, progress)
                }.filter { it.progress > 0 }

                UserStats(
                    factsRead = factsRead,
                    favoritesCount = favorites.size,
                    achievementsUnlocked = currentAchievements.count { it.status == AchievementStatus.COMPLETED },
                    level = level,
                    rankTitle = title,
                    streak = streak,
                    collectionProgress = activeCollections
                )
            }.collect {
                _stats.value = it
            }
        }
    }

    private fun calculateLevelAndRank(factsRead: Int): Pair<Int, String> {
        return when {
            factsRead >= 100 -> 10 to "Insight Master"
            factsRead >= 80 -> 9 to "Sage"
            factsRead >= 60 -> 8 to "Philosopher"
            factsRead >= 50 -> 7 to "Thinker"
            factsRead >= 40 -> 6 to "Researcher"
            factsRead >= 30 -> 5 to "Scholar"
            factsRead >= 20 -> 4 to "Explorer"
            factsRead >= 10 -> 3 to "Learner"
            factsRead >= 5 -> 2 to "Curious"
            else -> 1 to "Novice"
        }
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            combine(
                BiteRepository.getHistoryItems(),
                BiteRepository.getFavoriteFacts(getApplication()),
                BiteRepository.getSharesCount(),
                BiteRepository.getAllFacts(getApplication())
            ) { history, favorites, shares, allFacts ->
                AchievementManager.calculateAchievements(
                    historyItems = history,
                    favoritesCount = favorites.size,
                    sharesCount = shares,
                    allFacts = allFacts
                ).filter { it.status == AchievementStatus.COMPLETED }
            }.collect {
                _achievements.value = it
            }
        }
    }

    fun updateUserName(name: String) {
        PreferenceManager.setUserName(getApplication(), name)
    }

    fun updateProfile(name: String, bio: String, id: String, image: String) {
        PreferenceManager.setUserName(getApplication(), name)
        PreferenceManager.setUserBio(getApplication(), bio)
        PreferenceManager.setUserId(getApplication(), id)
        PreferenceManager.setUserImage(getApplication(), image)
    }

    fun updatePublicProfile(enabled: Boolean) {
        PreferenceManager.setPublicProfile(getApplication(), enabled)
    }

    fun updateAnalyticsEnabled(enabled: Boolean) {
        PreferenceManager.setAnalyticsEnabled(getApplication(), enabled)
    }
}
