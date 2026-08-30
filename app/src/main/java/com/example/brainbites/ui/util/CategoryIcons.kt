package com.example.brainbites.ui.util

import com.example.brainbites.R
import com.example.brainbites.data.BiteCategory
import com.example.brainbites.data.Category

fun BiteCategory.getIconDrawable(): Int {
    return when (this) {
        BiteCategory.ALL -> R.drawable.ic_cat_all
        BiteCategory.HUMAN_BEHAVIOR -> R.drawable.ic_cat_human_behavior
        BiteCategory.MENTAL_HEALTH -> R.drawable.ic_cat_mental_health
        BiteCategory.BRAIN_SCIENCE -> R.drawable.ic_cat_brain_science
        BiteCategory.LOVE_ATTRACTION -> R.drawable.ic_cat_love_attraction
        BiteCategory.PERSONALITY -> R.drawable.ic_cat_personality
        BiteCategory.BODY_LANGUAGE -> R.drawable.ic_cat_body_language
        BiteCategory.SUBCONSCIOUS -> R.drawable.ic_cat_subconscious
        BiteCategory.SOCIAL_PSYCHOLOGY -> R.drawable.ic_cat_social_psychology
        BiteCategory.HABITS_MOTIVATION -> R.drawable.ic_cat_habits_motivation
        BiteCategory.MEMORY_LEARNING -> R.drawable.ic_cat_memory_learning
    }
}

fun Category.getIconDrawable(): Int {
    // Try to find a matching drawable for the vectorIcon name
    return when (vectorIcon.lowercase()) {
        "brain" -> R.drawable.ic_cat_brain_science
        "users" -> R.drawable.ic_cat_human_behavior
        "heart" -> R.drawable.ic_cat_love_attraction
        "smile" -> R.drawable.ic_cat_personality
        "hand" -> R.drawable.ic_cat_body_language
        "waves" -> R.drawable.ic_cat_subconscious
        "globe" -> R.drawable.ic_cat_social_psychology
        "trendingup" -> R.drawable.ic_cat_habits_motivation
        "bookopen" -> R.drawable.ic_cat_memory_learning
        "zap" -> R.drawable.ic_cat_all
        else -> R.drawable.ic_cat_all // Fallback
    }
}
