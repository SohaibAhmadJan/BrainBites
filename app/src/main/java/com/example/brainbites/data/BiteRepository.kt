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
            // Human Behavior (1-15)
            "1" -> "discussion,explaining,people"
            "2" -> "mimicry,interaction,friends"
            "3" -> "stage,spotlight,performance"
            "4" -> "meeting,business,group"
            "5" -> "audience,watching,theater"
            "6" -> "confidence,smiling,person"
            "7" -> "waiting,queue,patience"
            "8" -> "crowd,help,emergency"
            "9" -> "agreement,handshake,deal"
            "10" -> "reflection,melancholy,thinking"
            "11" -> "decision,logic,brain"
            "12" -> "choices,variety,colorful"
            "13" -> "conformity,identical,group"
            "14" -> "commitment,stubborn,focus"
            "15" -> "storytelling,narrative,listeners"
            
            // Mental Health (16-30)
            "16" -> "stress,anxiety,mental"
            "17" -> "yoga,fitness,vitality"
            "18" -> "journal,writing,diary"
            "19" -> "solitude,lonely,reflection"
            "20" -> "emotion,feelings,talking"
            "21" -> "insomnia,tired,sleepy"
            "22" -> "purpose,direction,path"
            "23" -> "forest,woods,nature"
            "24" -> "burnout,exhausted,work"
            "25" -> "gratitude,heart,kindness"
            "26" -> "mask,hiding,emotions"
            "27" -> "support,empathy,friendship"
            "28" -> "smartphone,app,addiction"
            "29" -> "meditation,calm,peace"
            "30" -> "selfcare,hugging,kindness"
            
            // Brain Science (31-45)
            "31" -> "brain,electricity,energy"
            "32" -> "neurons,cells,growth"
            "33" -> "vision,eyes,optical"
            "34" -> "memory,brain,data"
            "35" -> "dreaming,sleeping,night"
            "36" -> "distraction,focus,multitasking"
            "37" -> "dopamine,reward,pleasure"
            "38" -> "hemispheres,bridge,logic"
            "39" -> "illusion,perception,art"
            "40" -> "screen,tablet,attention"
            "41" -> "prefrontal,youth,thinking"
            "42" -> "practice,mastery,skill"
            "43" -> "solving,puzzle,brain"
            "44" -> "empathy,mirror,connection"
            "45" -> "anatomy,brain,science"
            
            // Love & Attraction (46-60)
            "46" -> "adventure,couple,excitement"
            "47" -> "values,harmony,peace"
            "48" -> "appreciation,love,heart"
            "49" -> "gaze,staring,intimacy"
            "50" -> "passion,brain,reward"
            "51" -> "proximity,neighbors,friends"
            "52" -> "respect,communication,couple"
            "53" -> "complementary,balance,puzzle"
            "54" -> "hugging,bonding,oxytocin"
            "55" -> "celebrating,joy,success"
            "56" -> "distance,calling,romance"
            "57" -> "glance,first,attraction"
            "58" -> "laughter,happy,fun"
            "59" -> "symmetry,face,portrait"
            "60" -> "bonding,childhood,safety"
            
            // Personality (61-75)
            "61" -> "spectrum,traits,diversity"
            "62" -> "introvert,quiet,energy"
            "63" -> "order,organized,routine"
            "64" -> "flexible,change,adapt"
            "65" -> "creativity,painting,art"
            "66" -> "courage,bravery,mountain"
            "67" -> "compassion,helping,hand"
            "68" -> "handwriting,pen,style"
            "69" -> "wisdom,old,stable"
            "70" -> "agreeable,smiling,group"
            "71" -> "siblings,family,kids"
            "72" -> "social,adaptable,party"
            "73" -> "hobbies,leisure,fun"
            "74" -> "career,office,success"
            "75" -> "sensitive,quiet,feeling"
            
            // Body Language (76-90)
            "76" -> "crossed,defensive,arms"
            "77" -> "smile,eyes,happy"
            "78" -> "feet,walking,direction"
            "79" -> "mirroring,mimicking,friends"
            "80" -> "handshake,business,trust"
            "81" -> "confidence,looking,eye"
            "82" -> "nervous,touching,face"
            "83" -> "stance,powerful,pose"
            "84" -> "leaning,interested,person"
            "85" -> "listening,ear,curious"
            "86" -> "microexpression,face,emotion"
            "87" -> "pupils,eyes,macro"
            "88" -> "fidgeting,hands,fingers"
            "89" -> "walking,pavement,rhythm"
            "90" -> "palm,honest,open"
            
            // Subconscious (91-105)
            "91" -> "iceberg,ocean,subconscious"
            "92" -> "fast,visual,reaction"
            "93" -> "priming,subliminal,message"
            "94" -> "gut,instinct,feeling"
            "95" -> "familiarity,exposure,truth"
            "96" -> "solving,background,task"
            "97" -> "perfume,scent,memory"
            "98" -> "logic,thinking,brain"
            "99" -> "face,portrait,first"
            "100" -> "metronome,music,tempo"
            "101" -> "colors,palette,design"
            "102" -> "blindness,gap,missing"
            "103" -> "birthday,cake,calendar"
            "104" -> "weight,heavy,decision"
            "105" -> "sleeping,bed,dream"
            
            // Social Psychology (106-120)
            "106" -> "blame,failure,character"
            "107" -> "risk,gambling,group"
            "108" -> "milgram,obey,authority"
            "109" -> "uniform,costume,group"
            "110" -> "meeting,shaking,hands"
            "111" -> "nepotism,favor,group"
            "112" -> "announcement,public,goal"
            "113" -> "isolated,exclusion,lonely"
            "114" -> "helping,charity,person"
            "115" -> "speech,bubble,culture"
            "116" -> "hourglass,scarcity,limited"
            "117" -> "reviews,others,proof"
            "118" -> "puzzle,unfinished,task"
            "119" -> "anxiety,introvert,meeting"
            "120" -> "anonymous,mask,group"
            
            // Habits & Motivation (121-135)
            "121" -> "routine,morning,habit"
            "122" -> "painter,passion,internal"
            "123" -> "graph,wins,dopamine"
            "124" -> "ladder,steps,success"
            "125" -> "fruit,bowl,healthy"
            "126" -> "starting,runner,action"
            "127" -> "identity,who,runner"
            "128" -> "battery,fatigue,willpower"
            "129" -> "candy,reward,motivation"
            "130" -> "trainer,gym,accountable"
            "131" -> "procrastinating,stress,later"
            "132" -> "desk,environment,context"
            "133" -> "mindset,success,visualization"
            "134" -> "consistency,clock,habit"
            "135" -> "loss,missing,fear"
            
            // Memory & Learning (136-150)
            "136" -> "spacing,calendar,learning"
            "137" -> "exam,quiz,active"
            "138" -> "list,order,priority"
            "139" -> "reconstructing,thinking,memory"
            "140" -> "celebration,event,vivid"
            "141" -> "teaching,explaining,board"
            "142" -> "dancing,movement,learning"
            "143" -> "misremembering,error,brain"
            "144" -> "library,study,environment"
            "145" -> "nap,sleep,learning"
            "146" -> "blocks,units,memory"
            "147" -> "phone,texting,distracted"
            "148" -> "handwriting,pen,paper"
            "149" -> "forgetting,chart,time"
            "150" -> "child,question,curious"
            
            else -> "encyclopedia,discovery"
        }
    }

    private fun getBackupQuery(category: BiteCategory): String {
        return when (category) {
            BiteCategory.HUMAN_BEHAVIOR -> "people,social"
            BiteCategory.MENTAL_HEALTH -> "nature,peace"
            BiteCategory.BRAIN_SCIENCE -> "science,brain"
            BiteCategory.LOVE_ATTRACTION -> "love,couple"
            BiteCategory.PERSONALITY -> "face,portrait"
            BiteCategory.BODY_LANGUAGE -> "gesture,pose"
            BiteCategory.SUBCONSCIOUS -> "dream,mysterious"
            BiteCategory.SOCIAL_PSYCHOLOGY -> "society,crowd"
            BiteCategory.HABITS_MOTIVATION -> "goal,success"
            BiteCategory.MEMORY_LEARNING -> "study,books"
            else -> "educational,fact"
        }
    }

    private fun buildSecureImageUrl(id: String): String {
        // Switch to Picsum Photos for maximum reliability and 100% success rate
        // Using seed forces a unique image per ID while being extremely stable
        return "https://picsum.photos/seed/$id/1200/800"
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
                    // NEW: Switch to Picsum for rock-solid reliability
                    val imageUrl = buildSecureImageUrl(bite.id)

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
