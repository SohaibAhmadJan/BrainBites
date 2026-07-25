package com.example.brainbites.ui.facts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbites.data.BiteCategory
import com.example.brainbites.data.BiteItem
import com.example.brainbites.data.BiteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FactListViewModel(application: Application) : AndroidViewModel(application) {
    private val _facts = MutableStateFlow<List<BiteItem>>(emptyList())
    val facts = _facts.asStateFlow()

    fun loadFacts(categoryId: String) {
        viewModelScope.launch {
            val category = BiteCategory.valueOf(categoryId)
            BiteRepository.getAllFacts(getApplication()).collect { allFacts ->
                _facts.value = if (category == BiteCategory.ALL) {
                    allFacts
                } else {
                    allFacts.filter { it.category == category }
                }
            }
        }
    }

    fun toggleBookmark(id: String) {
        viewModelScope.launch {
            BiteRepository.toggleBookmark(getApplication(), id)
        }
    }
}
