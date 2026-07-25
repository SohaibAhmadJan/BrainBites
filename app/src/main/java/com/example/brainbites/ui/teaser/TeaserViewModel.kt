package com.example.brainbites.ui.teaser

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbites.data.BiteItem
import com.example.brainbites.data.BiteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeaserUiState(
    val teaserFact: BiteItem? = null,
    val isRevealed: Boolean = false,
    val isLoading: Boolean = true
)

class TeaserViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(TeaserUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTeaser()
    }

    fun loadTeaser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isRevealed = false) }
            BiteRepository.getAllFacts(getApplication()).collect { facts ->
                if (facts.isNotEmpty()) {
                    // Pick a random fact that has a question
                    val teaser = facts.filter { it.teaserType != null }.randomOrNull()
                    _uiState.update { it.copy(teaserFact = teaser, isLoading = false) }
                }
            }
        }
    }

    fun reveal() {
        _uiState.update { it.copy(isRevealed = true) }
    }
}
