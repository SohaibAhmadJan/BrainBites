package com.example.brainbites.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import com.example.brainbites.ui.home.PsychologyTip
import com.example.brainbites.ui.theme.AccentYellow
import com.example.brainbites.ui.theme.DarkGreenPrimary
import com.example.brainbites.ui.theme.GreenSecondary

@Composable
fun DailyTipCard(
    tip: PsychologyTip,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AccentYellow.copy(alpha = 0.4f),
                            GreenSecondary.copy(alpha = 0.15f)
                        )
                    )
                )
        ) {
            // Watermark Icon
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = AccentYellow.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 20.dp)
                    .graphicsLayer(rotationZ = -15f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = DarkGreenPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = DarkGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "BITE OF ADVICE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = DarkGreenPrimary,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkGreenPrimary
                )

                Text(
                    text = tip.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkGreenPrimary.copy(alpha = 0.8f),
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
