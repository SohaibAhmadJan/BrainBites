package com.example.brainbites.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbites.data.BiteCategory
import com.example.brainbites.data.BiteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryInfo(val category: BiteCategory, val count: Int)

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {
    private val _categories = MutableStateFlow<List<CategoryInfo>>(emptyList())
    val categories = _categories.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            BiteRepository.getAllFacts(getApplication()).collect { facts ->
                val list = BiteCategory.entries.filter { it != BiteCategory.ALL }.map { cat ->
                    CategoryInfo(cat, facts.count { it.category == cat })
                }
                _categories.value = list
            }
        }
    }
}
