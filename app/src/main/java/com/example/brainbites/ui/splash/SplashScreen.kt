package com.example.brainbites.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.brainbites.data.BiteRepository
import androidx.compose.ui.tooling.preview.Preview
import com.example.brainbites.ui.theme.BrainBitesTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainbites.ui.components.BrainBitesLogo
import com.example.brainbites.ui.components.AnimatedTagline
import com.example.brainbites.ui.util.TaglineManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Modern Splash Screen for BrainBites.
 * Features a sequence of animations for logo, name, and tagline.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    viewModel: SplashViewModel = viewModel(),
    isPreview: Boolean = false
) {
    val isSplashFinished by viewModel.isSplashFinished.collectAsState()

    // Animation States
    val logoAlpha = remember { Animatable(if (isPreview) 1f else 0f) }
    val nameAlpha = remember { Animatable(if (isPreview) 1f else 0f) }
    val taglineAlpha = remember { Animatable(if (isPreview) 1f else 0f) }
    val loadingAlpha = remember { Animatable(if (isPreview) 1f else 0f) }
    
    val logoScale = remember { Animatable(if (isPreview) 1f else 0.8f) }
    val logoTranslationY = remember { Animatable(if (isPreview) 0f else 40f) }

    // Navigation trigger removed from here as it's now handled in Animation Sequence

    val context = LocalContext.current

    // Animation Sequence
    LaunchedEffect(Unit) {
        if (isPreview) return@LaunchedEffect
        
        TaglineManager.refreshTagline()
        
        launch {
            delay(500)
            TaglineManager.triggerJump()
        }
        
        // Initialize Database while splash is showing
        launch {
            BiteRepository.initializeDatabase(context)
        }
        
        delay(300) 
        // Smooth continuous upward float start for logo
        launch {
            logoTranslationY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 2000)
            )
        }
        
        // Logo fade and scale
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(800))
        }
        launch {
            logoScale.animateTo(1f, animationSpec = tween(800))
        }
        
        delay(600)
        // Name fade
        launch {
            nameAlpha.animateTo(1f, animationSpec = tween(800))
        }
        
        delay(400)
        // Tagline fade
        launch {
            taglineAlpha.animateTo(1f, animationSpec = tween(800))
        }
        
        delay(300)
        // Loading fade in
        launch {
            loadingAlpha.animateTo(1f, animationSpec = tween(500))
        }

        delay(3500) // Stay until progress bar completes ~2 cycles
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Animated Logo (Floating)
            BrainBitesLogo(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer(
                        alpha = logoAlpha.value,
                        scaleX = logoScale.value,
                        scaleY = logoScale.value,
                        translationY = logoTranslationY.value
                    ),
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stable Name
            Text(
                text = "BrainBites",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp
                ),
                modifier = Modifier.graphicsLayer(alpha = nameAlpha.value)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Stable Linear Loading Indicator
            LinearProgressIndicator(
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                modifier = Modifier
                    .width(140.dp)
                    .height(4.dp)
                    .graphicsLayer(alpha = loadingAlpha.value)
                    .clip(RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.weight(1.2f))

            val currentTagline by TaglineManager.currentTagline.collectAsState()
            val jumpTrigger by TaglineManager.jumpTrigger.collectAsState()

            Box(
                modifier = Modifier
                    .graphicsLayer(alpha = taglineAlpha.value)
                    .padding(bottom = 12.dp)
            ) {
                AnimatedTagline(text = currentTagline, key = jumpTrigger)
            }

            Text(
                text = "Version 3.4.4",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    BrainBitesTheme {
        SplashScreen(
            onSplashFinished = {},
            isPreview = true
        )
    }
}

