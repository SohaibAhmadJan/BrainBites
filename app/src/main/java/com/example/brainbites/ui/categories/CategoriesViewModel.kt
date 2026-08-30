package com.example.brainbites.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbites.data.BiteItem
import com.example.brainbites.data.BiteRepository
import com.example.brainbites.data.Category
import com.example.brainbites.data.CollectionSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CategoryInfo(val category: Category, val count: Int)

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {
    private val _categories = MutableStateFlow<List<CategoryInfo>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _collections = MutableStateFlow<List<CollectionSet>>(emptyList())
    val collections = _collections.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _featuredFacts = MutableStateFlow<List<BiteItem>>(emptyList())
    val featuredFacts = _featuredFacts.asStateFlow()

    private val _searchResults = MutableStateFlow<List<BiteItem>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private var allFactsCache: List<BiteItem> = emptyList()
    private var allCategoriesCache: List<Category> = emptyList()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            combine(
                BiteRepository.getAllFacts(getApplication()),
                BiteRepository.getAllCategories()
            ) { facts, categories ->
                allFactsCache = facts
                allCategoriesCache = categories
                
                // Map Categories to Info with counts (Case-insensitive)
                categories.map { cat ->
                    CategoryInfo(cat, facts.count { 
                        it.category.equals(cat.name, ignoreCase = true) || 
                        it.category.equals(cat.id, ignoreCase = true) 
                    })
                }
            }.collect { list ->
                _categories.value = list
                
                // Update Featured Insights based on available categories
                if (allFactsCache.isNotEmpty()) {
                    val diverseFacts = allFactsCache.groupBy { it.category }
                        .values
                        .map { it.shuffled().first() }
                        .shuffled()
                        .take(5)
                    _featuredFacts.value = diverseFacts
                }
            }
        }

        viewModelScope.launch {
            BiteRepository.getAllCollections().collect { sets ->
                _collections.value = sets
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            val tokens = query.lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
            _searchResults.value = allFactsCache.filter { item ->
                tokens.all { token ->
                    item.fact.contains(token, ignoreCase = true) ||
                    item.category.contains(token, ignoreCase = true) ||
                    (item.keywords?.contains(token, ignoreCase = true) ?: false)
                }
            }
        }
    }

    fun getRandomFactId(): String? {
        return allFactsCache.randomOrNull()?.id
    }
}
