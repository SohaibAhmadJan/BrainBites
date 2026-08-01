package com.example.brainbites.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.ui.graphics.vector.ImageVector

fun getCollectionIcon(collectionId: String): ImageVector {
    return when (collectionId) {
        "persuasion_kit" -> Icons.Default.AdsClick
        "social_mastery" -> Icons.Default.Handshake
        "mindfulness_basics" -> Icons.Default.SelfImprovement
        "habit_loop" -> Icons.Default.Loop
        else -> Icons.Default.QuestionMark
    }
}
