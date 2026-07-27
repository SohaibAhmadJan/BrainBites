package com.example.brainbites.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.lazy.LazyColumn
import android.app.TimePickerDialog
import com.example.brainbites.data.theme.ThemeManager
import com.example.brainbites.ui.util.ShareUtils
import com.example.brainbites.ui.theme.BrainBitesTheme
import com.example.brainbites.ui.theme.ThemeMode

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val currentTheme by ThemeManager.themeMode.collectAsState()
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
                    Text("Version 2.5", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "BrainBites is your daily companion for psychology facts, mental puzzles, and habit-building insights. Our mission is to make learning about the human mind accessible and engaging for everyone.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text("© 2026 BrainBites Team", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("General", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        
        item {
            SettingsToggleItem(
                title = "Daily Notifications",
                subtitle = "Receive a new psychology fact every day",
                icon = Icons.Default.Notifications,
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }
        
        item {
            SettingsActionItem(
                title = "Notification Time",
                subtitle = selectedTime,
                icon = Icons.Default.Schedule,
                onClick = { timePickerDialog.show() }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        item {
            Text("Appearance", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        
        item {
            ThemeSelector(
                selectedMode = currentTheme,
                onModeSelected = { ThemeManager.setTheme(context, it) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        item {
            Text("App", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }

        item { SettingsActionItem(title = "Rate App", icon = Icons.Default.Star, onClick = { ShareUtils.rateApp(context) }) }
        item { SettingsActionItem(title = "Share App", icon = Icons.Default.Share, onClick = { ShareUtils.shareApp(context) }) }
        item { SettingsActionItem(title = "About BrainBites", icon = Icons.Default.Info, onClick = { showAboutDialog = true }) }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun ThemeSelector(selectedMode: ThemeMode, onModeSelected: (ThemeMode) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("App Theme", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Row {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = mode == selectedMode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onModeSelected(mode) },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    if (mode != ThemeMode.entries.last()) Spacer(Modifier.width(4.dp))
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
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
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
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                subtitle?.let {
                    Text(it, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
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
