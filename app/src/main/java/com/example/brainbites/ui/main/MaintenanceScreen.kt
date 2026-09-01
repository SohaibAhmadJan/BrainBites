package com.example.brainbites.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainbites.ui.components.BrainBitesLogo
import com.example.brainbites.ui.theme.DeepForest
import com.example.brainbites.ui.theme.PureWhite
import com.example.brainbites.ui.theme.DarkSurface
import com.example.brainbites.ui.theme.SageGreen

@Composable
fun MaintenanceScreen(
    message: String
) {
    val backgroundColor = DeepForest
    val surfaceColor = DarkSurface
    val accentColor = SageGreen

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor,
                            backgroundColor.copy(alpha = 0.9f),
                            Color(0xFF0C1420) // Deep dark blueish tint at bottom
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Logo Container
                Surface(
                    modifier = Modifier.size(140.dp),
                    shape = CircleShape,
                    color = surfaceColor.copy(alpha = 0.3f),
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        BrainBitesLogo(
                            modifier = Modifier.size(80.dp),
                            color = accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "Mindful Updates",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = PureWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message.ifBlank { "We are making small improvements to your experience. We will be back in just a few minutes." },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 26.sp
                    ),
                    color = PureWhite.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
