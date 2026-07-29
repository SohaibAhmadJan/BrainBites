package com.example.brainbites.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.*
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

@Serializable
data class CollectionSet(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val color: String,
    val factIds: List<String>
)

@Serializable
private data class CollectionWrapper(val collections: List<CollectionSet>)

@Serializable
data class HistoryItem(val factId: String, val timestamp: Long)

object BiteRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val _bites = MutableStateFlow<List<BiteItem>>(emptyList())
    private val _collections = MutableStateFlow<List<CollectionSet>>(emptyList())
    private val favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    private val historyItems = MutableStateFlow<List<HistoryItem>>(emptyList())
    private val _sharesCount = MutableStateFlow(0)
    
    private const val PREFS_NAME = "brain_bites_prefs"
    private const val FAVORITES_KEY = "favorite_ids"
    private const val HISTORY_KEY_V2 = "history_items_json_v2"
    private const val SHARES_KEY = "shares_count"

    private fun getSearchQuery(id: String): String {
        return when (id) {
            "1" -> "persuasion,influence"
            "2" -> "mimicry,connection"
            "3" -> "awareness,spotlight"
            "4" -> "group,decision"
            "5" -> "audience,performance"
            "6" -> "confidence,smile"
            "7" -> "waiting,patience"
            "8" -> "crowd,help"
            "9" -> "agreement,trust"
            "10" -> "memory,reflection"
            "11" -> "brain,logic"
            "12" -> "choice,variety"
            "13" -> "conformity,group"
            "14" -> "mind,stubborn"
            "15" -> "storytelling,narrative"
            "16" -> "brain,stress"
            "17" -> "exercise,vitality"
            "18" -> "writing,clarity"
            "19" -> "solitude,reflection"
            "20" -> "emotion,expression"
            "21" -> "tired,sleep"
            "22" -> "purpose,path"
            "23" -> "nature,forest"
            "24" -> "work,pressure"
            "25" -> "gratitude,heart"
            "26" -> "mask,emotion"
            "27" -> "support,friendship"
            "28" -> "phone,addiction"
            "29" -> "breathing,peace"
            "30" -> "kindness,self"
            "31" -> "brain,energy"
            "32" -> "neuron,network"
            "33" -> "visual,learning"
            "34" -> "memory,focus"
            "35" -> "sleep,rest"
            "36" -> "focus,distraction"
            "37" -> "reward,dopamine"
            "38" -> "brain,logic,creative"
            "39" -> "perception,vision"
            "40" -> "digital,device"
            "41" -> "growth,mindset"
            "42" -> "practice,mastery"
            "43" -> "problem,solving"
            "44" -> "empathy,connection"
            "45" -> "hippocampus,anatomy"
            "46" -> "bond,adventure"
            "47" -> "values,harmony"
            "48" -> "appreciation,love"
            "49" -> "gaze,intimacy"
            "50" -> "passion,brain"
            "51" -> "loyalty,friend"
            "52" -> "conflict,resolution"
            "53" -> "personality,balance"
            "54" -> "comfort,warmth"
            "55" -> "joy,celebration"
            "56" -> "connection,distance"
            "57" -> "glance,instant"
            "58" -> "happiness,laugh"
            "59" -> "face,symmetry"
            "60" -> "bond,safety"
            "61" -> "spectrum,traits"
            "62" -> "introvert,extrovert"
            "63" -> "discipline,order"
            "64" -> "flexibility,change"
            "65" -> "art,creation"
            "66" -> "strength,courage"
            "67" -> "compassion,helping"
            "68" -> "signature,style"
            "69" -> "wisdom,smile"
            "70" -> "kindness,helping"
            "71" -> "sibling,family"
            "72" -> "adaptability,social"
            "73" -> "hobby,sharing"
            "74" -> "work,fulfillment"
            "75" -> "sensitive,quiet"
            "76" -> "posture,defense"
            "77" -> "smile,eyes"
            "78" -> "direction,interest"
            "79" -> "synchrony,gesture"
            "80" -> "professional,handshake"
            "81" -> "focus,eye"
            "82" -> "nerves,anxiety"
            "83" -> "confidence,stance"
            "84" -> "interest,leaning"
            "85" -> "listening,curiosity"
            "86" -> "blink,expression"
            "87" -> "eyes,dilation"
            "88" -> "restless,hands"
            "89" -> "walking,rhythm"
            "100" -> "music,beats"
            "101" -> "colors,design"
            "102" -> "vision,detail"
            "103" -> "birthday,date"
            "104" -> "brain,feelings"
            "105" -> "sleeping,dreams"
            "106" -> "blame,error"
            "107" -> "risk,group"
            "108" -> "authority,obey"
            "109" -> "team,uniform"
            "110" -> "meeting,shaking"
            "111" -> "preference,bias"
            "112" -> "goals,public"
            "113" -> "alone,crowd"
            "114" -> "help,person"
            "115" -> "culture,speech"
            "116" -> "watch,time"
            "117" -> "leadership,follow"
            "118" -> "puzzle,work"
            "119" -> "greeting,stranger"
            "120" -> "mask,anonymous"
            "121" -> "habit,routine"
            "122" -> "painter,passion"
            "123" -> "success,chart"
            "124" -> "ladder,climb"
            "125" -> "fruit,vegetable"
            "126" -> "action,start"
            "127" -> "identity,who"
            "128" -> "tired,mind"
            "129" -> "sweet,food"
            "130" -> "gym,partner"
            "131" -> "clock,wasting"
            "132" -> "environment,habit"
            "133" -> "mind,success"
            "134" -> "nature,growth"
            "135" -> "money,loss"
            "136" -> "card,repetition"
            "137" -> "exam,testing"
            "138" -> "list,order"
            "139" -> "brain,reconstruct"
            "140" -> "event,memory"
            "150" -> "child,explore"
            else -> "psychology"
        }
    }

    private fun getBackupQuery(category: BiteCategory): String {
        return when (category) {
            BiteCategory.HUMAN_BEHAVIOR -> "behavior"
            BiteCategory.MENTAL_HEALTH -> "wellness"
            BiteCategory.BRAIN_SCIENCE -> "neuroscience"
            BiteCategory.LOVE_ATTRACTION -> "love"
            BiteCategory.PERSONALITY -> "personality"
            BiteCategory.BODY_LANGUAGE -> "bodylanguage"
            BiteCategory.SUBCONSCIOUS -> "subconscious"
            BiteCategory.SOCIAL_PSYCHOLOGY -> "social"
            BiteCategory.HABITS_MOTIVATION -> "motivation"
            BiteCategory.MEMORY_LEARNING -> "learning"
            else -> "psychology"
        }
    }

    suspend fun initializeDatabase(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Load favorites
        val savedIds = prefs.getStringSet(FAVORITES_KEY, emptySet()) ?: emptySet()
        favoriteIds.value = savedIds.toSet()

        // Load History V2 (with timestamps)
        val historyJson = prefs.getString(HISTORY_KEY_V2, "[]") ?: "[]"
        try {
            historyItems.value = json.decodeFromString<List<HistoryItem>>(historyJson)
        } catch (e: Exception) {
            historyItems.value = emptyList()
        }

        // Load Shares
        _sharesCount.value = prefs.getInt(SHARES_KEY, 0)

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
                    
                    val query = getSearchQuery(bite.id)
                    val backup = getBackupQuery(bite.category)
                    // Combining specific query with a lock to ensure absolute uniqueness and relevance
                    val imageUrl = "https://loremflickr.com/1200/800/$query,$backup?lock=${bite.id}"

                    bite.copy(
                        quizQuestion = quiz?.question,
                        quizOptions = quiz?.options,
                        correctAnswerIndex = quiz?.correctIndex,
                        teaserType = quiz?.teaserType,
                        imageUrl = imageUrl,
                        keywords = query
                    )
                }
                
                _bites.value = mergedBites

                // Load Collections
                val collectionsString = context.assets.open("collections.json").bufferedReader().use { it.readText() }
                val collectionWrapper = json.decodeFromString<CollectionWrapper>(collectionsString)
                _collections.value = collectionWrapper.collections
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAllCollections(): Flow<List<CollectionSet>> = _collections.asStateFlow()

    fun getCollection(id: String): CollectionSet? = _collections.value.find { it.id == id }

    fun getFactsForCollection(collectionId: String): Flow<List<BiteItem>> {
        return combine(_collections, _bites, favoriteIds, historyItems) { collections, bites, favIds, history ->
            val collection = collections.find { it.id == collectionId } ?: return@combine emptyList()
            val ids = collection.factIds.toSet()
            val historyIds = history.map { it.factId }.toSet()
            
            bites.filter { it.id in ids }.map { item ->
                item.copy(
                    isBookmarked = favIds.contains(item.id),
                    isCompleted = historyIds.contains(item.id)
                )
            }
        }
    }

    fun getCollectionProgress(collectionId: String): Flow<Float> {
        return combine(_collections, historyItems) { collections, history ->
            val collection = collections.find { it.id == collectionId } ?: return@combine 0f
            val readIds = history.map { it.factId }.toSet()
            val collectionIds = collection.factIds.toSet()
            val intersection = collectionIds.intersect(readIds)
            if (collectionIds.isEmpty()) 0f else intersection.size.toFloat() / collectionIds.size.toFloat()
        }
    }

    fun getAllFacts(context: Context): Flow<List<BiteItem>> {
        return combine(_bites, favoriteIds, historyItems) { list, ids, history ->
            val historyIds = history.map { it.factId }.toSet()
            list.map { item ->
                item.copy(
                    isBookmarked = ids.contains(item.id),
                    isCompleted = historyIds.contains(item.id)
                )
            }
        }
    }

    fun getFavoriteFacts(context: Context): Flow<List<BiteItem>> {
        return getAllFacts(context).map { list ->
            list.filter { it.isBookmarked }
        }
    }

    fun getHistoryFacts(context: Context): Flow<List<BiteItem>> {
        return combine(_bites, historyItems, favoriteIds) { bites, items, favs ->
            items.mapNotNull { item ->
                bites.find { it.id == item.factId }?.copy(isBookmarked = favs.contains(item.factId))
            }
        }
    }

    fun getHistoryItems(): Flow<List<HistoryItem>> = historyItems.asStateFlow()

    fun getSharesCount(): Flow<Int> = _sharesCount.asStateFlow()

    suspend fun incrementShares(context: Context) {
        val newVal = _sharesCount.value + 1
        _sharesCount.value = newVal
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(SHARES_KEY, newVal) }
    }

    suspend fun addToHistory(context: Context, id: String) {
        val current = historyItems.value.toMutableList()
        current.removeAll { it.factId == id }
        current.add(0, HistoryItem(id, System.currentTimeMillis()))
        val limited = current.take(50)
        historyItems.value = limited

        // Trigger streak update
        PreferenceManager.updateStreak(context)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(HISTORY_KEY_V2, json.encodeToString(limited)) }
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
        
        // Update the reactive state
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
