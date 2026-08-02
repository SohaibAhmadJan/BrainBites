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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import kotlin.math.cos
import kotlin.math.sin

/**
 * A highly efficient, subtle animated background that gives the app a "breathing" feel.
 * Uses a slow-moving radial gradient that transitions between theme-aware forest tones.
 */
@Composable
fun LiveBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    // 1. Slow rhythmic pulse for the gradient center
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // 2. Subtle color shift to add "life"
    val glowColor by infiniteTransition.animateColor(
        initialValue = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        targetValue = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = SineWaveEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowColor"
    )

    val backgroundColor = MaterialTheme.colorScheme.background

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Calculate a shifting center based on a circular path
        val centerX = width / 2f + (width / 4f) * cos(time)
        val centerY = height / 2f + (height / 6f) * sin(time)

        // Draw the base solid background
        drawRect(color = backgroundColor)

        // Draw the moving "breathing" glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor, Color.Transparent),
                center = Offset(centerX, centerY),
                radius = width * 1.5f
            ),
            radius = width * 1.5f,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * Custom Easing for a natural "breathing" feel.
 */
private val SineWaveEasing = Easing { fraction ->
    ((1 - cos(fraction * Math.PI)) / 2).toFloat()
}
