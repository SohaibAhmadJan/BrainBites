package com.example.brainbites.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainbites.data.BiteItem
import com.example.brainbites.data.BiteRepository
import com.example.brainbites.data.Category
import com.example.brainbites.ui.theme.*

@Composable
fun QuoteCard(
    fact: BiteItem,
    modifier: Modifier = Modifier
) {
    val isInPreview = LocalInspectionMode.current

    val categoryInfo = remember(fact.category) {
        if (isInPreview) {
            Category("human_behavior", fact.category.ifBlank { "Human Behavior" }, "👥", "👥", "#A8DADC", "", 0)
        } else {
            try {
                BiteRepository.resolveCategory(fact.category)
            } catch (_: Exception) {
                Category("human_behavior", fact.category.ifBlank { "Human Behavior" }, "👥", "👥", "#A8DADC", "", 0)
            }
        }
    }

    Box(
        modifier = modifier
            .size(1080.dp) // Fixed 1080x1080 canvas for high-res social media export
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            )
    ) {
        // Main Inner Card
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(64.dp), // Outer padding for the card
            shape = RoundedCornerShape(48.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(64.dp), // Inner padding
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP: Category Badge
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = categoryInfo.name.uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        letterSpacing = 2.sp
                    )
                }

                // MIDDLE: Quote Text
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FormatQuote,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x = (-16).dp), // Slight offset to align the quote marks with the text
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    
                    Text(
                        text = fact.fact.ifBlank { "The mere presence of other people can improve performance on simple, well-practiced tasks and hurt performance on complex, unfamiliar ones." },
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 46.sp,
                        lineHeight = 64.sp,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // BOTTOM: Branding Footer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BrainBitesLogo(
                        modifier = Modifier.size(80.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Column {
                        Text(
                            text = "BRAINBITES",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Feed Your Mind Daily.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 400)
@Composable
fun QuoteCardPreview() {
    BrainBitesTheme {
        val sampleFact = BiteItem(
            id = "1",
            fact = "The mere presence of other people can improve performance on simple, well-practiced tasks.",
            category = "Human Behavior"
        )
        
        // This wrapper scales the massive 1080x1080 card down to fit inside the 400x400 preview window
        Box(
            modifier = Modifier
                .size(400.dp)
                .background(Color.Gray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(1080.dp) // The actual card size
                    .graphicsLayer(
                        scaleX = 400f / 1080f,
                        scaleY = 400f / 1080f
                    )
            ) {
                QuoteCard(fact = sampleFact)
            }
        }
    }
}
