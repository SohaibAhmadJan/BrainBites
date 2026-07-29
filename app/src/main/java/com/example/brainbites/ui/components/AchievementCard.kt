package com.example.brainbites.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.brainbites.data.Achievement
import com.example.brainbites.data.AchievementManager
import com.example.brainbites.data.AchievementStatus
import com.example.brainbites.ui.theme.AccentYellow
import com.example.brainbites.ui.theme.DarkGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementCard(
    achievement: Achievement,
    modifier: Modifier = Modifier
) {
    var showDetail by remember { mutableStateOf(false) }
    val isCompleted = achievement.status == AchievementStatus.COMPLETED

    if (showDetail) {
        AlertDialog(
            onDismissRequest = { showDetail = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = achievement.icon,
                        contentDescription = null,
                        tint = if (isCompleted) AccentYellow else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = achievement.title, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = achievement.description, style = MaterialTheme.typography.bodyLarge)
                    
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Insight: \"${AchievementManager.getAchievementInsight(achievement.id)}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetail = false }) {
                    Text("Got it")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    // State dependent styling
    val cardAlpha = 1.0f

    val cardBorder = when {
        isCompleted -> BorderStroke(1.5.dp, AccentYellow)
        else -> null
    }

    val containerColor = when {
        isCompleted -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
    }

    val iconBackgroundColor = when {
        isCompleted -> AccentYellow.copy(alpha = 0.25f)
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val iconTint = when {
        isCompleted -> DarkGreenPrimary
        else -> MaterialTheme.colorScheme.primary
    }

    val progressTrackColor = when {
        isCompleted -> AccentYellow.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val progressIndicatorColor = when {
        isCompleted -> AccentYellow
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .width(280.dp)
            .alpha(cardAlpha)
            .animateContentSize(),
        onClick = { showDetail = true },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = achievement.icon,
                        contentDescription = achievement.title,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Description
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCompleted) "Completed" else "Progress",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Text(
                        text = achievement.progressText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) DarkGreenPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { achievement.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressIndicatorColor,
                    trackColor = progressTrackColor
                )
            }
        }
    }
}
