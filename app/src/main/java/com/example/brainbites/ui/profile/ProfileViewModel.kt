package com.example.brainbites.ui.profile

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbites.data.Achievement
import com.example.brainbites.data.AchievementStatus
import com.example.brainbites.data.BiteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UserStats(
    val factsRead: Int = 0,
    val favoritesCount: Int = 0,
    val achievementsUnlocked: Int = 0
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _stats = MutableStateFlow(UserStats())
    val stats = _stats.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements = _achievements.asStateFlow()

    init {
        loadStats()
        loadAchievements()
    }

    private fun loadStats() {
        viewModelScope.launch {
            combine(
                BiteRepository.getHistoryFacts(getApplication()),
                BiteRepository.getFavoriteFacts(getApplication())
            ) { history, favorites ->
                UserStats(
                    factsRead = history.size,
                    favoritesCount = favorites.size,
                    achievementsUnlocked = 1 // Placeholder for now
                )
            }.collect {
                _stats.value = it
            }
        }
    }

    private fun loadAchievements() {
        // Shared logic with HomeViewModel for consistency
        _achievements.value = listOf(
            Achievement(
                id = "1",
                title = "First Quote Read",
                description = "Knowledge begins with a single step.",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                currentProgress = 1,
                maxProgress = 1,
                status = AchievementStatus.COMPLETED
            ),
            Achievement(
                id = "2",
                title = "Scholar",
                description = "Read 10 quotes to unlock.",
                icon = Icons.Default.EmojiEvents,
                currentProgress = 4,
                maxProgress = 10,
                status = AchievementStatus.IN_PROGRESS
            ),
            Achievement(
                id = "3",
                title = "Mindful",
                description = "Check in for 7 days straight",
                icon = Icons.Default.Psychology,
                currentProgress = 2,
                maxProgress = 7,
                status = AchievementStatus.LOCKED
            )
        )
    }
}
