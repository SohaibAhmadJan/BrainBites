package com.example.brainbites.ui.home

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbites.data.Achievement
import com.example.brainbites.data.AchievementStatus
import com.example.brainbites.data.BiteItem
import com.example.brainbites.data.BiteRepository
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

    init {
        loadFacts()
        loadHistory()
        loadAchievements()
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

    fun selectMood(mood: String) {
        _selectedMood.value = mood
    }

    fun toggleBookmark(id: String) {
        viewModelScope.launch {
            BiteRepository.toggleBookmark(getApplication(), id)
        }
    }
}
