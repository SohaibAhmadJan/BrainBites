package com.example.brainbites.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A premium, purely native animated background.
 * Creates a "Living Emerald" forest effect with multiple drifting mist layers.
 */
@Composable
fun LivingEmeraldBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "living_forest")

    // 1. Primary "Breathing" Pulse (Slow)
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // 2. Mist Layer 1: Slow Drifter
    val mistOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mist1"
    )

    // 3. Mist Layer 2: Faster Drifter
    val mistOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mist2"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val backgroundColor = MaterialTheme.colorScheme.background

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Background Base
        drawRect(color = backgroundColor)

        // Layer 1: Deep Slow Mist (Top-Right focus)
        val center1 = Offset(
            x = width * 0.8f + (width * 0.1f) * cos(mistOffset1),
            y = height * 0.2f + (height * 0.05f) * sin(mistOffset1)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = pulseAlpha), Color.Transparent),
                center = center1,
                radius = width * 1.2f
            ),
            radius = width * 1.2f,
            center = center1
        )

        // Layer 2: Vibrant Mid Mist (Bottom-Left focus)
        val center2 = Offset(
            x = width * 0.2f + (width * 0.15f) * sin(mistOffset2),
            y = height * 0.7f + (height * 0.1f) * cos(mistOffset2)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = pulseAlpha * 0.8f), Color.Transparent),
                center = center2,
                radius = width * 1.5f
            ),
            radius = width * 1.5f,
            center = center2
        )

        // Layer 3: Accent Glow (Center breathing)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondaryColor.copy(alpha = 0.04f), Color.Transparent),
                center = center2,
                radius = width * 0.8f
            ),
            radius = width * 0.8f,
            center = Offset(width / 2f, height / 2f)
        )
    }
}
