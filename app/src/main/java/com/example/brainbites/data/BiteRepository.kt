package com.example.brainbites.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar

@Serializable
private data class FactsWrapper(val facts: List<BiteItem>)

@Serializable
private data class QuizItem(
    val factId: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val teaserType: String
)

@Serializable
private data class QuizWrapper(val quizzes: List<QuizItem>)

object BiteRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val _bites = MutableStateFlow<List<BiteItem>>(emptyList())
    private val favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    private val historyIds = MutableStateFlow<List<String>>(emptyList())
    
    private const val PREFS_NAME = "brain_bites_prefs"
    private const val FAVORITES_KEY = "favorite_ids"
    private const val HISTORY_KEY = "history_ids_json"

    suspend fun initializeDatabase(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Load favorites
        val savedIds = prefs.getStringSet(FAVORITES_KEY, emptySet()) ?: emptySet()
        favoriteIds.value = savedIds.toSet()

        // Load History
        val historyJson = prefs.getString(HISTORY_KEY, "[]") ?: "[]"
        try {
            historyIds.value = json.decodeFromString<List<String>>(historyJson)
        } catch (e: Exception) {
            historyIds.value = emptyList()
        }

        if (_bites.value.isEmpty()) {
            try {
                // Load Facts
                val factsString = context.assets.open("facts.json").bufferedReader().use { it.readText() }
                val factsWrapper = json.decodeFromString<FactsWrapper>(factsString)
                
                // Load Quizzes
                val quizString = context.assets.open("quiz_data.json").bufferedReader().use { it.readText() }
                val quizWrapper = json.decodeFromString<QuizWrapper>(quizString)
                val quizMap = quizWrapper.quizzes.associateBy { it.factId }

                // Merge Data
                val mergedBites = factsWrapper.facts.map { bite ->
                    val quiz = quizMap[bite.id]
                    
                    // Category-based human-centric keywords for high-relevance imagery
                    val imageKeyword = when (bite.category) {
                        BiteCategory.HUMAN_BEHAVIOR -> "person,expression,mind"
                        BiteCategory.MENTAL_HEALTH -> "serene,nature,meditation"
                        BiteCategory.BRAIN_SCIENCE -> "science,microscope,technology"
                        BiteCategory.LOVE_ATTRACTION -> "couple,romance,together"
                        BiteCategory.PERSONALITY -> "creativity,portrait,expression"
                        BiteCategory.BODY_LANGUAGE -> "handshake,gesture,conversation"
                        BiteCategory.SUBCONSCIOUS -> "dreamy,stars,fantasy"
                        BiteCategory.SOCIAL_PSYCHOLOGY -> "community,crowd,meeting"
                        BiteCategory.HABITS_MOTIVATION -> "workout,achievement,planning"
                        BiteCategory.MEMORY_LEARNING -> "education,study,thinking"
                        else -> "psychology,thinking"
                    }
                    // High-resolution for modern displays
                    val imageUrl = "https://loremflickr.com/1200/800/$imageKeyword?lock=${bite.id}"

                    bite.copy(
                        quizQuestion = quiz?.question,
                        quizOptions = quiz?.options,
                        correctAnswerIndex = quiz?.correctIndex,
                        teaserType = quiz?.teaserType,
                        imageUrl = imageUrl
                    )
                }
                
                _bites.value = mergedBites
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAllFacts(context: Context): Flow<List<BiteItem>> {
        return combine(_bites, favoriteIds) { list, ids ->
            list.map { item ->
                item.copy(isBookmarked = ids.contains(item.id))
            }
        }
    }

    fun getFavoriteFacts(context: Context): Flow<List<BiteItem>> {
        return getAllFacts(context).map { list ->
            list.filter { it.isBookmarked }
        }
    }

    fun getHistoryFacts(context: Context): Flow<List<BiteItem>> {
        return combine(_bites, historyIds, favoriteIds) { bites, ids, favs ->
            ids.mapNotNull { id ->
                bites.find { it.id == id }?.copy(isBookmarked = favs.contains(id))
            }
        }
    }

    suspend fun addToHistory(context: Context, id: String) {
        val current = historyIds.value.toMutableList()
        current.remove(id)
        current.add(0, id)
        val limited = current.take(20)
        historyIds.value = limited

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(HISTORY_KEY, json.encodeToString(limited)) }
    }

    suspend fun toggleBookmark(context: Context, id: String) {
        val current = favoriteIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putStringSet(FAVORITES_KEY, current) }
        
        favoriteIds.value = current
    }
    
    fun getFactOfTheDay(allFacts: List<BiteItem>): BiteItem? {
        if (allFacts.isEmpty()) return null
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        val index = (dayOfYear + year) % allFacts.size
        val fact = allFacts[index]
        return fact.copy(isBookmarked = favoriteIds.value.contains(fact.id))
    }
}
