package com.example.brainbites.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.example.brainbites.data.PreferenceManager

/**
 * Triggers a direct physical hardware vibration on real Android phones.
 */
fun triggerPhysicalVibration(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            val vibrator = vibratorManager?.defaultVibrator
            vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(30)
        }
    } catch (e: Exception) {
        // Fallback / ignore if device lacks motor or permission error
    }
}

/**
 * A custom modifier that adds a premium "Elastic Bounce" scale effect
 * and an "Inner Glow" overlay when the element is pressed.
 */
fun Modifier.premiumClickable(
    enabled: Boolean = true,
    glowColor: Color = Color.White,
    scaleDownBy: Float = 0.04f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val context = LocalContext.current
    val hapticsEnabled by PreferenceManager.hapticsEnabled.collectAsState()

    // 1. Elastic Bounce (Scale) Animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1f - scaleDownBy else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "premiumScale"
    )

    // 2. Inner Glow (Alpha) Animation
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.12f else 0f,
        animationSpec = tween(durationMillis = 100),
        label = "premiumGlow"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null, // Disable default ripple to use our custom glow
            enabled = enabled,
            onClick = {
                if (hapticsEnabled && enabled) {
                    triggerPhysicalVibration(context)
                }
                onClick()
            }
        )
        .background(glowColor.copy(alpha = glowAlpha))
}
