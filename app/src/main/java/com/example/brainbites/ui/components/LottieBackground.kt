package com.example.brainbites.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
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

    // --- Misty Forest "Serenity" Refinement ---
    
    val backgroundColor = MaterialTheme.colorScheme.background
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    
    val isDarkMode = MaterialTheme.colorScheme.background.toArgb().let { 
        it == 0xFF0F1F17.toInt()
    }

    val uniqueM5 = if (isDarkMode) Color(0xFF4A7C59) else Color(0xFF84A98C)
    val uniqueM6 = if (isDarkMode) Color(0xFF32533D) else Color(0xFF52796F)

    // --- MANUALLY SET VALUES HERE ---
    val landOpacity = 100 // Forest elements opacity (0 to 100)
    val backgroundScrimOpacity = 0.28f // Transparent sheet opacity (0.0f to 1.0f)
    // --------------------------------

    val dynamicProperties = rememberLottieDynamicProperties(
        // 1. SKY: Match app background perfectly
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(backgroundColor.toArgb()),
            keyPath = arrayOf("Shape Layer 1", "**")
        ),
        
        // 2. SUN/MOON: Brand Gold (Solid)
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(tertiaryColor.toArgb()),
            keyPath = arrayOf("moon", "**")
        ),
        
        // 3. CLOUDS: Subtle theme-aware ghosting (Solid)
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
        ),

        // 4. TREES: Mist-like Faded Presence (Opacity 20)
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("tree1", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(primaryColor.toArgb()), keyPath = arrayOf("tree1", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("tree2", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(secondaryColor.toArgb()), keyPath = arrayOf("tree2", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("tree3", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(primaryColor.toArgb()), keyPath = arrayOf("tree3", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("tree4", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(secondaryColor.toArgb()), keyPath = arrayOf("tree4", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("tree5", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(primaryColor.toArgb()), keyPath = arrayOf("tree5", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("tree6", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(secondaryColor.toArgb()), keyPath = arrayOf("tree6", "**")),
        
        // 5. MOUNTAINS (Opacity 20 + Colors)
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("m1", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(primaryContainerColor.toArgb()), keyPath = arrayOf("m1", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("m2", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(primaryContainerColor.toArgb()), keyPath = arrayOf("m2", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("m3", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(secondaryColor.toArgb()), keyPath = arrayOf("m3", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("m4", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(secondaryColor.toArgb()), keyPath = arrayOf("m4", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("m5", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(uniqueM5.toArgb()), keyPath = arrayOf("m5", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("m6", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(uniqueM6.toArgb()), keyPath = arrayOf("m6", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("m31", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(secondaryContainerColor.toArgb()), keyPath = arrayOf("m31", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("m34", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(secondaryContainerColor.toArgb()), keyPath = arrayOf("m34", "**")),
        
        // 6. ATMOSPHERIC SHAPES
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("Shape Layer 4", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(surfaceVariantColor.toArgb()), keyPath = arrayOf("Shape Layer 4", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("Shape Layer 11", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(surfaceVariantColor.toArgb()), keyPath = arrayOf("Shape Layer 11", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("Shape Layer 12", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(surfaceVariantColor.toArgb()), keyPath = arrayOf("Shape Layer 12", "**")),
        
        rememberLottieDynamicProperty(property = LottieProperty.OPACITY, value = landOpacity, keyPath = arrayOf("Shape Layer 13", "**")),
        rememberLottieDynamicProperty(property = LottieProperty.COLOR_FILTER, value = SimpleColorFilter(surfaceVariantColor.toArgb()), keyPath = arrayOf("Shape Layer 13", "**"))
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            dynamicProperties = dynamicProperties
        )
        
        // Transparent sheet covering the entire background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor.copy(alpha = backgroundScrimOpacity))
        )
    }
}
