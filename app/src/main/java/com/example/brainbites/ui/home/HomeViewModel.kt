package com.example.brainbites.ui.home

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbites.data.Achievement
import com.example.brainbites.data.AchievementManager
import com.example.brainbites.data.AchievementRepository
import com.example.brainbites.data.AchievementStatus
import com.example.brainbites.data.AuthRepository
import com.example.brainbites.data.BiteItem
import com.example.brainbites.data.BiteRepository
import com.example.brainbites.data.Notification
import com.example.brainbites.data.NotificationRepository
import com.example.brainbites.data.NotificationType
import com.example.brainbites.data.PreferenceManager
import com.example.brainbites.data.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val _allFacts = MutableStateFlow<List<BiteItem>>(emptyList())
    val allFacts = _allFacts.asStateFlow()

    private val _rotatingFactId = MutableStateFlow<String?>(null)
    
    // Combine full list and active ID to make the Hero Card reactive to bookmark changes
    val rotatingFact = combine(_allFacts, _rotatingFactId) { facts, id ->
        facts.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _recentlyViewed = MutableStateFlow<List<BiteItem>>(emptyList())
    val recentlyViewed = _recentlyViewed.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements = _achievements.asStateFlow()

    private val _selectedMood = MutableStateFlow<String?>(null)
    val selectedMood = _selectedMood.asStateFlow()

    private val _moodMessage = MutableStateFlow<String?>(null)
    val moodMessage = _moodMessage.asStateFlow()

    private val _dailyTip = MutableStateFlow<PsychologyTip?>(null)
    val dailyTip = _dailyTip.asStateFlow()

    init {
        loadFacts()
        loadHistory()
        loadAchievements()
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            SettingsRepository.settings.collect { settings ->
                _dailyTip.value = PsychologyTip(settings.dailyTipTitle, settings.dailyTipMessage)
                if (_rotatingFactId.value == null && _allFacts.value.isNotEmpty()) {
                    _rotatingFactId.value = settings.featuredFactId
                }
            }
        }
    }

    private fun loadFacts() {
        viewModelScope.launch {
            BiteRepository.getAllFacts(getApplication()).collect { facts ->
                _allFacts.value = facts
                if (_rotatingFactId.value == null && facts.isNotEmpty()) {
                    _rotatingFactId.value = BiteRepository.getFactOfTheDay(facts)?.id
                }
            }
        }
        
        // Start automatic rotation
        viewModelScope.launch {
            while (true) {
                delay(12000L)
                val currentFacts = _allFacts.value
                if (currentFacts.isNotEmpty()) {
                    _rotatingFactId.value = currentFacts.random().id
                }
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            BiteRepository.getHistoryFacts(getApplication()).collect { history ->
                _recentlyViewed.value = history.take(2)
            }
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
                )
            }.collect { currentAchievements ->
                _achievements.value = currentAchievements
                
                // Sync with Firestore
                AuthRepository.currentUser.value?.let { user ->
                    currentAchievements.forEach { achievement ->
                        if (achievement.status != AchievementStatus.LOCKED) {
                            viewModelScope.launch {
                                AchievementRepository.updateProgress(
                                    user.account.uid, 
                                    achievement.id, 
                                    achievement.currentProgress
                                )
                            }
                        }
                    }
                }

                // Monitor for newly completed achievements
                val alreadyNotified = PreferenceManager.notifiedAchievements.value
                currentAchievements.forEach { achievement ->
                    if (achievement.status == AchievementStatus.COMPLETED && 
                        !alreadyNotified.contains(achievement.id)) {
                        
                        // 1. Add to Notifications
                        NotificationRepository.addNotification(
                            Notification(
                                id = "ach_${achievement.id}_${System.currentTimeMillis()}",
                                title = "Milestone Reached!",
                                message = "Congratulations! You've unlocked '${achievement.title}'.",
                                timestamp = System.currentTimeMillis(),
                                type = NotificationType.ACHIEVEMENT
                            )
                        )
                        
                        // 2. Mark as notified persistently
                        PreferenceManager.markAchievementAsNotified(getApplication(), achievement.id)
                    }
                }
            }
        }
    }

    fun selectMood(mood: String) {
        if (_selectedMood.value == mood) {
            _selectedMood.value = null
            _moodMessage.value = null
            return
        }

        _selectedMood.value = mood
        
        val (category, message) = when (mood) {
            "😊 Happy" -> com.example.brainbites.data.BiteCategory.LOVE_ATTRACTION to "Keep spreading the joy! Here's something about connection."
            "😌 Calm" -> com.example.brainbites.data.BiteCategory.BODY_LANGUAGE to "Peace is power. Discover the language of serenity."
            "😔 Sad" -> com.example.brainbites.data.BiteCategory.MENTAL_HEALTH to "It's okay to feel. Here's a bite of mental wellness for you."
            "😤 Stressed" -> com.example.brainbites.data.BiteCategory.MENTAL_HEALTH to "Take a deep breath. Let's look at how the mind handles pressure."
            "💡 Motivated" -> com.example.brainbites.data.BiteCategory.HABITS_MOTIVATION to "Fuel your fire! Here's a tip on habits and drive."
            else -> com.example.brainbites.data.BiteCategory.HUMAN_BEHAVIOR to "Curiosity is the best mood! Explore this insight."
        }

        _moodMessage.value = message

        // Immediately update rotating fact to match mood
        val matchingFacts = _allFacts.value.filter { it.category == category }
        if (matchingFacts.isNotEmpty()) {
            _rotatingFactId.value = matchingFacts.random().id
        }
    }

    fun toggleBookmark(id: String) {
        viewModelScope.launch {
            BiteRepository.toggleBookmark(getApplication(), id)
        }
    }
}

data class PsychologyTip(val title: String, val message: String)
