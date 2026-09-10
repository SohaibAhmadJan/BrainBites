package com.example.brainbites.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainbites.data.SettingsRepository
import com.example.brainbites.ui.components.AnimatedEntrance
import com.example.brainbites.ui.components.LottieBackground
import com.example.brainbites.ui.theme.DeepForest
import com.example.brainbites.ui.theme.PureWhite
import com.example.brainbites.ui.theme.DarkSurface

@Composable
fun AccountDisabledScreen() {
    val settings by SettingsRepository.settings.collectAsState()
    
    // Theme-aware colors
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.toArgb() == DeepForest.toArgb()
    
    val scrimColor = colorScheme.background.copy(alpha = if (isDark) 0.65f else 0.4f)

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Immersive Background Layer
        LottieBackground()
        
        // 2. Adaptive Scrim Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor)
        )

        // 3. Central Floating Card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedEntrance(index = 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    border = BorderStroke(1.dp, colorScheme.error.copy(alpha = if (isDark) 0.2f else 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Restriction Icon
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = colorScheme.error.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = colorScheme.error,
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Access Restricted",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Your access to BrainBites has been restricted by the administration. All your learning progress remains safely preserved.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 26.sp
                            ),
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(0.6f),
                            thickness = 1.dp,
                            color = colorScheme.outline.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "BrainBites v${settings.latestVersion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurface.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Stay Curious. — BrainBites Team",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            color = colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Light Mode", showBackground = true)
@Composable
fun AccountDisabledScreenLightPreview() {
    com.example.brainbites.ui.theme.BrainBitesTheme(themeMode = com.example.brainbites.ui.theme.ThemeMode.LIGHT) {
        AccountDisabledScreen()
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Dark Mode", showBackground = true)
@Composable
fun AccountDisabledScreenDarkPreview() {
    com.example.brainbites.ui.theme.BrainBitesTheme(themeMode = com.example.brainbites.ui.theme.ThemeMode.DARK) {
        AccountDisabledScreen()
    }
}
