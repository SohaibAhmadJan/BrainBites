package com.example.brainbites.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BiteItem(
    val id: String,
    val fact: String,
    val category: BiteCategory,
    val title: String? = null,
    val snippet: String? = null,
    val fullFact: String? = null,
    val whyItMatters: String? = null,
    val quizQuestion: String? = null,
    val quizOptions: List<String>? = null,
    val correctAnswerIndex: Int? = null,
    val teaserType: String? = null,
    val imageUrl: String? = null,
    val readTimeMinutes: Int = 1,
    var isBookmarked: Boolean = false,
    var isCompleted: Boolean = false
)

@Serializable
enum class BiteCategory(val displayName: String, val iconRes: String, val colorHex: String) {
    @SerialName("All") ALL("All", "✨", "#F1FAEE"),
    @SerialName("Human Behavior") HUMAN_BEHAVIOR("Human Behavior", "👥", "#A8DADC"),
    @SerialName("Mental Health") MENTAL_HEALTH("Mental Health", "🧠", "#457B9D"),
    @SerialName("Brain Science") BRAIN_SCIENCE("Brain Science", "🧪", "#E9C46A"),
    @SerialName("Love & Attraction") LOVE_ATTRACTION("Love & Attraction", "💖", "#E76F51"),
    @SerialName("Personality Traits") PERSONALITY("Personality Traits", "🎭", "#F4A261"),
    @SerialName("Body Language") BODY_LANGUAGE("Body Language", "✋", "#2A9D8F"),
    @SerialName("Subconscious Mind") SUBCONSCIOUS("Subconscious Mind", "🌌", "#264653"),
    @SerialName("Social Psychology") SOCIAL_PSYCHOLOGY("Social Psychology", "🏘️", "#8AB17D"),
    @SerialName("Habits & Motivation") HABITS_MOTIVATION("Habits & Motivation", "📈", "#B5838D"),
    @SerialName("Memory & Learning") MEMORY_LEARNING("Memory & Learning", "📚", "#6D6875")
}
