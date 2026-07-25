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
import com.example.brainbites.data.theme.ThemeManager
import com.example.brainbites.ui.theme.BrainBitesTheme
import com.example.brainbites.ui.theme.ThemeMode

@Composable
fun SettingsScreen(isVisible: Boolean = true) {
    val context = LocalContext.current
    val currentTheme by ThemeManager.themeMode.collectAsState()
    var notificationsEnabled by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("General", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            
            SettingsToggleItem(
                title = "Daily Notifications",
                subtitle = "Receive a new psychology fact every day",
                icon = Icons.Default.Notifications,
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
            
            SettingsActionItem(
                title = "Notification Time",
                subtitle = "9:00 AM",
                icon = Icons.Default.Schedule,
                onClick = { /* Show Time Picker */ }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Appearance", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            
            ThemeSelector(
                selectedMode = currentTheme,
                onModeSelected = { ThemeManager.setTheme(context, it) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("App", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)

            SettingsActionItem(title = "Rate App", icon = Icons.Default.Star, onClick = { })
            SettingsActionItem(title = "Share App", icon = Icons.Default.Share, onClick = { })
            SettingsActionItem(title = "About BrainBites", icon = Icons.Default.Info, onClick = { })
        }
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
        SettingsScreen(isVisible = true)
    }
}
