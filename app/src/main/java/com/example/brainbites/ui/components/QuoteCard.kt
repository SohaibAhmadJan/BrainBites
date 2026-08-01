package com.example.brainbites.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainbites.data.BiteCategory
import com.example.brainbites.data.BiteItem
import com.example.brainbites.ui.theme.*

@Composable
fun QuoteCard(
    fact: BiteItem,
    modifier: Modifier = Modifier
) {
    val categoryColor = Color(android.graphics.Color.parseColor(fact.category.colorHex))
    
    Box(
        modifier = modifier
            .size(1080.dp, 1080.dp) // Standard Square Social Media Size
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        categoryColor.copy(alpha = 0.2f)
                    )
                )
            )
            .padding(60.dp)
    ) {
        // Branding - Top
        Row(
            modifier = Modifier.align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrainBitesLogo(modifier = Modifier.size(40.dp), color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "BRAINBITES",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Main Quote
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "“",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 120.sp,
                color = categoryColor,
                fontWeight = FontWeight.Black
            )
            
            Text(
                text = fact.fact,
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 48.sp,
                lineHeight = 64.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                color = categoryColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = fact.category.displayName.uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Tagline - Bottom
        Text(
            text = "Feed Your Mind Daily.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.BottomCenter),
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuoteCardPreview() {
    BrainBitesTheme {
        val sampleFact = BiteItem(
            id = "1",
            fact = "Humans tend to mimic the body language of people they're comfortable with.",
            category = BiteCategory.HUMAN_BEHAVIOR
        )
        QuoteCard(fact = sampleFact)
    }
}
