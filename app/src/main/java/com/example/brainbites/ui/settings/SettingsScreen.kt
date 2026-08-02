package com.example.brainbites.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.lazy.LazyColumn
import android.app.TimePickerDialog
import com.example.brainbites.data.PreferenceManager
import com.example.brainbites.ui.components.AnimatedEntrance
import com.example.brainbites.data.theme.ThemeManager
import com.example.brainbites.ui.util.ShareUtils
import com.example.brainbites.ui.util.ExportUtils
import com.example.brainbites.ui.theme.BrainBitesTheme
import com.example.brainbites.ui.theme.ThemeMode
import com.example.brainbites.data.BiteRepository

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val currentTheme by ThemeManager.themeMode.collectAsState()
    val dailyGoal by PreferenceManager.dailyGoal.collectAsState()
    val textScale by PreferenceManager.textScale.collectAsState()
    val hapticsEnabled by PreferenceManager.hapticsEnabled.collectAsState()
    val favorites by BiteRepository.getFavoriteFacts(context).collectAsState(initial = emptyList())

    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedTime by remember { mutableStateOf("09:00 AM") }
    var showAboutDialog by remember { mutableStateOf(false) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = if (hour == 0 || hour == 12) 12 else hour % 12
            selectedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm)
        },
        9, 0, false
    )

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About BrainBites", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Version 3.4.2", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "BrainBites is your daily companion for psychology facts, mental puzzles, and habit-building insights. Our mission is to make learning about the human mind accessible and engaging for everyone.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Credits", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Avatars provided by DiceBear",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("© 2026 BrainBites Team", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 150.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AnimatedEntrance(index = 0) {
                Text("General", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
        
        item {
            AnimatedEntrance(index = 1) {
                SettingsToggleItem(
                    title = "Daily Notifications",
                    subtitle = "Receive a new psychology fact every day",
                    icon = Icons.Default.Notifications,
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
        }
        
        item {
            AnimatedEntrance(index = 2) {
                SettingsActionItem(
                    title = "Notification Time",
                    subtitle = selectedTime,
                    icon = Icons.Default.Schedule,
                    onClick = { timePickerDialog.show() }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        item {
            AnimatedEntrance(index = 3) {
                Text("Personalized Goals", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        item {
            AnimatedEntrance(index = 4) {
                GoalSelector(
                    selectedGoal = dailyGoal,
                    onGoalSelected = { PreferenceManager.setDailyGoal(context, it) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        item {
            AnimatedEntrance(index = 5) {
                Text("Appearance & Accessibility", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
        
        item {
            AnimatedEntrance(index = 6) {
                ThemeSelector(
                    selectedMode = currentTheme,
                    onModeSelected = { ThemeManager.setTheme(context, it) }
                )
            }
        }

        item {
            AnimatedEntrance(index = 7) {
                TextScaleSelector(
                    currentScale = textScale,
                    onScaleChanged = { PreferenceManager.setTextScale(context, it) }
                )
            }
        }

        item {
            AnimatedEntrance(index = 8) {
                SettingsToggleItem(
                    title = "Haptic Feedback",
                    subtitle = "Subtle vibrations during interactions",
                    icon = Icons.Default.Vibration,
                    checked = hapticsEnabled,
                    onCheckedChange = { PreferenceManager.setHapticsEnabled(context, it) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            AnimatedEntrance(index = 9) {
                Text("Data & Portability", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        item {
            AnimatedEntrance(index = 10) {
                SettingsActionItem(
                    title = "Export My Favorites",
                    subtitle = "Save your insights as a text file",
                    icon = Icons.Default.SaveAlt,
                    onClick = { ExportUtils.exportFavoritesToText(context, favorites) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        item {
            AnimatedEntrance(index = 11) {
                Text("App", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        item { 
            AnimatedEntrance(index = 12) {
                SettingsActionItem(title = "Rate App", icon = Icons.Default.Star, onClick = { ShareUtils.rateApp(context) }) 
            }
        }
        item { 
            AnimatedEntrance(index = 13) {
                SettingsActionItem(title = "Share App", icon = Icons.Default.Share, onClick = { ShareUtils.shareApp(context) }) 
            }
        }
        item { 
            AnimatedEntrance(index = 14) {
                SettingsActionItem(title = "About BrainBites", icon = Icons.Default.Info, onClick = { showAboutDialog = true }) 
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun GoalSelector(selectedGoal: Int, onGoalSelected: (Int) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Daily Reading Goal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val goals = listOf(3, 5, 10, 20)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                goals.chunked(2).forEach { rowGoals ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowGoals.forEach { goal ->
                            FilterChip(
                                selected = goal == selectedGoal,
                                onClick = { onGoalSelected(goal) },
                                label = { 
                                    Text(
                                        text = "$goal Facts", 
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelSmall
                                    ) 
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextScaleSelector(currentScale: Float, onScaleChanged: (Float) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TextFormat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Text Scaling", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = currentScale,
                onValueChange = onScaleChanged,
                valueRange = 0.8f..1.4f,
                steps = 2,
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("A", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Normal", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("A", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ThemeSelector(selectedMode: ThemeMode, onModeSelected: (ThemeMode) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("App Theme", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = mode == selectedMode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onModeSelected(mode) },
                        label = { 
                            Text(
                                text = mode.name.lowercase().replaceFirstChar { it.uppercase() }, 
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsToggleItem(title: String, subtitle: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun SettingsActionItem(title: String, subtitle: String? = null, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                subtitle?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    BrainBitesTheme {
        SettingsScreen()
    }
}
