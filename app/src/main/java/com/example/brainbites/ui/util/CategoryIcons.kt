package com.example.brainbites.ui.util

import com.example.brainbites.R
import com.example.brainbites.data.BiteCategory

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
