package com.example.brainbites.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

    init {
        loadFacts()
        loadHistory()
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

    fun toggleBookmark(id: String) {
        viewModelScope.launch {
            BiteRepository.toggleBookmark(getApplication(), id)
        }
    }
}
