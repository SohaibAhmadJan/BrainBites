package com.example.brainbites.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainbites.ui.util.TaglineManager
import kotlinx.coroutines.delay

@Composable
fun BrandHeader(
    title: String
) {
    val currentTagline by TaglineManager.currentTagline.collectAsState()
    val jumpTrigger by TaglineManager.jumpTrigger.collectAsState()

    Column {
        AnimatedTagline(text = currentTagline, key = jumpTrigger)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (title == "BrainBites") {
                BrainBitesLogo(
                    modifier = Modifier.size(28.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 22.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BrandHeaderPreview() {
    com.example.brainbites.ui.theme.BrainBitesTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            BrandHeader(title = "BrainBites")
            Spacer(modifier = Modifier.height(16.dp))
            BrandHeader(title = "Explore")
        }
    }
}

@Composable
fun AnimatedTagline(text: String, key: Any) {
    Row {
        var triggerAnimation by remember { mutableStateOf(false) }
        
        LaunchedEffect(key) {
            triggerAnimation = false
            delay(100)
            triggerAnimation = true
        }

        text.forEachIndexed { index, char ->
            val animatedOffset = remember { Animatable(0f) }
            
            if (char == ' ') {
                Text(
                    text = " ",
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                LaunchedEffect(triggerAnimation) {
                    if (triggerAnimation) {
                        delay(index * 30L) 
                        animatedOffset.animateTo(
                            targetValue = -6f,
                            animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
                        )
                        animatedOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                        )
                    } else {
                        animatedOffset.snapTo(0f)
                    }
                }
                
                Text(
                    text = char.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.graphicsLayer {
                        translationY = animatedOffset.value
                    }
                )
            }
        }
    }
}
