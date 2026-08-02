package com.example.brainbites.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.example.brainbites.R

@Composable
fun LottieBackground() {
    val compositionResult = rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.background)
    )
    val composition by compositionResult
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    // --- Premium Multi-Shade "Forest Serenity" Refinement ---
    
    val backgroundColor = MaterialTheme.colorScheme.background
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    val dynamicProperties = rememberLottieDynamicProperties(
        // SKY: Match app background
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(backgroundColor.toArgb()),
            keyPath = arrayOf("Shape Layer 1", "**")
        ),
        
        // SUN/MOON: Brand Gold
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(tertiaryColor.toArgb()),
            keyPath = arrayOf("moon", "**")
        ),
        
        // TREES: Explicit targeting for depth
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(primaryColor.toArgb()),
            keyPath = arrayOf("tree1", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(primaryColor.toArgb()),
            keyPath = arrayOf("tree3", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(primaryColor.toArgb()),
            keyPath = arrayOf("tree5", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(secondaryColor.toArgb()),
            keyPath = arrayOf("tree2", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(secondaryColor.toArgb()),
            keyPath = arrayOf("tree4", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(secondaryColor.toArgb()),
            keyPath = arrayOf("tree6", "**")
        ),
        
        // MOUNTAIN LAYERING (EXPLICIT TARGETING)
        
        // Close Mountains -> Lush Green (Primary Container)
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(primaryContainerColor.toArgb()),
            keyPath = arrayOf("m1", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(primaryContainerColor.toArgb()),
            keyPath = arrayOf("m2", "**")
        ),
        
        // Mid Mountains -> Misty Green (Secondary)
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(secondaryColor.toArgb()),
            keyPath = arrayOf("m3", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(secondaryColor.toArgb()),
            keyPath = arrayOf("m4", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(secondaryColor.toArgb()),
            keyPath = arrayOf("m5", "**")
        ),
        
        // Far Mountains -> Pale Sage (Secondary Container)
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(secondaryContainerColor.toArgb()),
            keyPath = arrayOf("m6", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(secondaryContainerColor.toArgb()),
            keyPath = arrayOf("m31", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(secondaryContainerColor.toArgb()),
            keyPath = arrayOf("m34", "**")
        ),
        
        // Background Detail Shapes -> Soft Forest Air (Surface Variant)
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(surfaceVariantColor.toArgb()),
            keyPath = arrayOf("Shape Layer 4", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(surfaceVariantColor.toArgb()),
            keyPath = arrayOf("Shape Layer 11", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(surfaceVariantColor.toArgb()),
            keyPath = arrayOf("Shape Layer 12", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(surfaceVariantColor.toArgb()),
            keyPath = arrayOf("Shape Layer 13", "**")
        ),
        
        // CLOUDS: Ghostly white/onBackground (Subtle)
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(onBackgroundColor.copy(alpha = 0.08f).toArgb()),
            keyPath = arrayOf("c1", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(onBackgroundColor.copy(alpha = 0.08f).toArgb()),
            keyPath = arrayOf("c2", "**")
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(onBackgroundColor.copy(alpha = 0.08f).toArgb()),
            keyPath = arrayOf("c3", "**")
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            dynamicProperties = dynamicProperties
        )
    }
}
