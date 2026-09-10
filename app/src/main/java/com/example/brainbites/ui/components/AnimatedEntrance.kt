package com.example.brainbites.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

import androidx.compose.ui.platform.LocalInspectionMode

/**
 * A shared component to provide a staggered "bubble" entrance effect.
 * Slides up, scales in, and fades in based on the provided index.
 */
@Composable
fun AnimatedEntrance(
    index: Int,
    delayMultiplier: Long = 50L,
    content: @Composable () -> Unit
) {
    val isPreview = LocalInspectionMode.current
    val animatedProgress = remember { Animatable(if (isPreview) 1f else 0f) }

    LaunchedEffect(Unit) {
        if (!isPreview) {
            delay(index * delayMultiplier)
            animatedProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Box(
        modifier = Modifier.graphicsLayer {
            alpha = animatedProgress.value
            translationY = (1f - animatedProgress.value) * 60.dp.toPx()
            scaleX = 0.95f + (animatedProgress.value * 0.05f)
            scaleY = 0.95f + (animatedProgress.value * 0.05f)
        }
    ) {
        content()
    }
}
