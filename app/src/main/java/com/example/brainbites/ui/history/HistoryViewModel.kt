package com.example.brainbites.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbites.data.BiteItem
import com.example.brainbites.data.BiteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val _history = MutableStateFlow<List<BiteItem>>(emptyList())
    val history = _history.asStateFlow()

    init {
        viewModelScope.launch {
            BiteRepository.getHistoryFacts(getApplication()).collect {
                _history.value = it
            }
        }
    }

    fun toggleBookmark(id: String) {
        viewModelScope.launch {
            BiteRepository.toggleBookmark(getApplication(), id)
        }
    }
}
