package com.example.brainbites.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class AchievementStatus {
    LOCKED,
    IN_PROGRESS,
    COMPLETED
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val status: AchievementStatus,
    val icon: ImageVector = Icons.Default.Star
) {
    val progressFraction: Float
        get() = if (maxProgress > 0) (currentProgress.toFloat() / maxProgress.toFloat()).coerceIn(0f, 1f) else 0f

    val progressText: String
        get() = "$currentProgress/$maxProgress"
}
