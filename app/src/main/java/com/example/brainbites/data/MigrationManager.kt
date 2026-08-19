package com.example.brainbites.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class FactsWrapperMigrate(val facts: List<BiteItem>)

@Serializable
private data class QuizItemMigrate(
    val factId: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val teaserType: String
)

@Serializable
private data class QuizWrapperMigrate(val quizzes: List<QuizItemMigrate>)

@Serializable
private data class CollectionWrapperMigrate(val collections: List<CollectionSet>)

object MigrationManager {
    private val db = FirebaseFirestore.getInstance()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun performMigration(context: Context) {
        Log.d("MigrationManager", "Starting migration...")
        
        try {
            migrateFactsAndQuizzes(context)
            migrateCollections(context)
            migrateCategories()
            migrateAchievements()
            setupGlobalConfig()
            Log.d("MigrationManager", "Migration completed successfully")
        } catch (e: Exception) {
            Log.e("MigrationManager", "Migration failed", e)
        }
    }

    private suspend fun migrateFactsAndQuizzes(context: Context) {
        val factsString = context.assets.open("facts.json").bufferedReader().use { it.readText() }
        val factsWrapper = json.decodeFromString<FactsWrapperMigrate>(factsString)

        val quizString = context.assets.open("quiz_data.json").bufferedReader().use { it.readText() }
        val quizWrapper = json.decodeFromString<QuizWrapperMigrate>(quizString)
        val quizMap = quizWrapper.quizzes.associateBy { it.factId }

        val batch = db.batch()
        val factsCollection = db.collection("facts")
        val quizzesCollection = db.collection("quizzes")

        factsWrapper.facts.forEach { bite ->
            val factRef = factsCollection.document(bite.id)
            val quiz = quizMap[bite.id]

            val factData = mapOf(
                "fact" to bite.fact,
                "title" to (bite.title ?: "Psychology Insight"),
                "category" to bite.category.displayName,
                "categoryId" to bite.category.name,
                "snippet" to bite.snippet,
                "fullFact" to bite.fullFact,
                "whyItMatters" to bite.whyItMatters,
                "imageUrl" to "https://picsum.photos/seed/${bite.id}/1200/800",
                "keywords" to (bite.keywords ?: ""),
                "readTimeMinutes" to bite.readTimeMinutes,
                "isPublished" to true,
                "isFeatured" to false,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            batch.set(factRef, factData)

            if (quiz != null) {
                val quizRef = quizzesCollection.document(bite.id)
                val quizData = mapOf(
                    "factId" to bite.id,
                    "question" to quiz.question,
                    "options" to quiz.options,
                    "correctAnswerIndex" to quiz.correctIndex,
                    "teaserType" to quiz.teaserType,
                    "isActive" to true,
                    "createdAt" to System.currentTimeMillis()
                )
                batch.set(quizRef, quizData)
            }
        }
        
        batch.commit().await()
        Log.d("MigrationManager", "Facts and Quizzes migrated")
    }

    private suspend fun migrateCollections(context: Context) {
        val collectionsString = context.assets.open("collections.json").bufferedReader().use { it.readText() }
        val collectionWrapper = json.decodeFromString<CollectionWrapperMigrate>(collectionsString)
        
        val batch = db.batch()
        val collRef = db.collection("collections")

        collectionWrapper.collections.forEach { collection ->
            val docRef = collRef.document(collection.id)
            val data = mapOf(
                "title" to collection.title,
                "description" to collection.description,
                "icon" to collection.icon,
                "color" to collection.color,
                "factIds" to collection.factIds,
                "isPublished" to true,
                "createdAt" to System.currentTimeMillis()
            )
            batch.set(docRef, data)
        }

        batch.commit().await()
        Log.d("MigrationManager", "Collections migrated")
    }

    private suspend fun migrateCategories() {
        val batch = db.batch()
        val catRef = db.collection("categories")

        BiteCategory.values().forEach { category ->
            val docRef = catRef.document(category.name)
            val data = mapOf(
                "name" to category.displayName,
                "icon" to category.iconRes,
                "colorHex" to category.colorHex,
                "isActive" to true,
                "sortOrder" to category.ordinal
            )
            batch.set(docRef, data)
        }

        batch.commit().await()
        Log.d("MigrationManager", "Categories migrated")
    }

    private suspend fun migrateAchievements() {
        val batch = db.batch()
        val achRef = db.collection("achievements")

        val list = listOf(
            mapOf("id" to "first_step", "title" to "First Step", "description" to "Read your very first psychology fact.", "maxProgress" to 1, "iconName" to "DirectionsRun", "requirementType" to "READ_COUNT"),
            mapOf("id" to "scholar", "title" to "The Scholar", "description" to "Read 10 unique psychology facts.", "maxProgress" to 10, "iconName" to "MenuBook", "requirementType" to "READ_COUNT"),
            mapOf("id" to "curator", "title" to "The Curator", "description" to "Save 5 facts to your favorites.", "maxProgress" to 5, "iconName" to "Favorite", "requirementType" to "FAVORITE_COUNT"),
            mapOf("id" to "explorer", "title" to "The Explorer", "description" to "Discover facts from 5 different categories.", "maxProgress" to 5, "iconName" to "Explore", "requirementType" to "CATEGORY_COUNT"),
            mapOf("id" to "thinker", "title" to "The Thinker", "description" to "Read 50 unique facts.", "maxProgress" to 50, "iconName" to "Psychology", "requirementType" to "READ_COUNT"),
            mapOf("id" to "socialite", "title" to "Socialite", "description" to "Share 3 facts with friends.", "maxProgress" to 3, "iconName" to "Share", "requirementType" to "SHARE_COUNT"),
            mapOf("id" to "master", "title" to "Master of Mind", "description" to "Read 100 total facts.", "maxProgress" to 100, "iconName" to "AutoAwesome", "requirementType" to "READ_COUNT")
        )

        list.forEach { item ->
            val docRef = achRef.document(item["id"] as String)
            val data = item.toMutableMap()
            data["isActive"] = true
            data["createdAt"] = System.currentTimeMillis()
            batch.set(docRef, data)
        }

        batch.commit().await()
        Log.d("MigrationManager", "Achievements migrated")
    }

    private suspend fun setupGlobalConfig() {
        val configRef = db.collection("app_config").document("global")
        val config = mapOf(
            "maintenanceMode" to false,
            "minVersion" to "1.0.0",
            "latestVersion" to "3.4.8.7",
            "quizzesEnabled" to true,
            "achievementsEnabled" to true,
            "dailyFactId" to "1",
            "updatedAt" to System.currentTimeMillis()
        )
        configRef.set(config).await()
        Log.d("MigrationManager", "Global config setup")
    }
}
