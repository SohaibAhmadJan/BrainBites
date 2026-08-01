package com.example.brainbites.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainbites.ui.components.AnimatedEntrance
import com.example.brainbites.ui.components.AchievementCard
import com.example.brainbites.ui.theme.AccentYellow
import com.example.brainbites.ui.theme.DarkGreenPrimary
import com.example.brainbites.ui.theme.GreenSecondary

@Composable
fun ProfileScreen(
    onCollectionClick: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val isPublic by viewModel.isPublicProfile.collectAsState()
    val isAnalytics by viewModel.isAnalyticsEnabled.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = userName,
            onDismiss = { showEditDialog = false },
            onConfirm = { 
                viewModel.updateUserName(it)
                showEditDialog = false
            }
        )
    }

    if (showPrivacyDialog) {
        PrivacySettingsDialog(
            isPublic = isPublic,
            isAnalytics = isAnalytics,
            onDismiss = { showPrivacyDialog = false },
            onPublicToggle = { viewModel.updatePublicProfile(it) },
            onAnalyticsToggle = { viewModel.updateAnalyticsEnabled(it) }
        )
    }

    ProfileScreenContent(
        stats = stats,
        achievements = achievements,
        userName = userName,
        onEditClick = { showEditDialog = true },
        onPrivacyClick = { showPrivacyDialog = true },
        onCollectionClick = onCollectionClick
    )
}

@Composable
fun ProfileScreenContent(
    stats: UserStats,
    achievements: List<com.example.brainbites.data.Achievement>,
    userName: String,
    onEditClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onCollectionClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Header: Avatar & Name
        item {
            AnimatedEntrance(index = 0) {
                ProfileHeader(
                    userName = userName,
                    level = stats.level,
                    rankTitle = stats.rankTitle,
                    streak = stats.streak
                )
            }
        }

        // 2. Stats Row
        item {
            AnimatedEntrance(index = 1) {
                StatsSection(stats)
            }
        }

        // 3. Achievements Section
        item {
            AnimatedEntrance(index = 2) {
                AchievementsSection(achievements)
            }
        }

        // 3.5 Collections Progress (NEW)
        if (stats.collectionProgress.isNotEmpty()) {
            item {
                AnimatedEntrance(index = 3) {
                    CollectionProgressSection(
                        progressList = stats.collectionProgress,
                        onCollectionClick = onCollectionClick
                    )
                }
            }
        }

        // 4. Quick Settings / Actions
        item {
            AnimatedEntrance(index = 4) {
                ProfileActionSection(
                    onEditClick = onEditClick,
                    onPrivacyClick = onPrivacyClick
                )
            }
        }
        
        item { Spacer(modifier = Modifier.height(112.dp)) }
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    level: Int,
    rankTitle: String,
    streak: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Level $level • $rankTitle",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            if (streak > 0) {
                Surface(
                    color = Color(0xFFFFF7ED),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDBA74))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "$streak Day Streak",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC2410C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsSection(stats: UserStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Facts Read",
            value = stats.factsRead.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Favorites",
            value = stats.favoritesCount.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Unlocked",
            value = stats.achievementsUnlocked.toString(),
            modifier = Modifier.weight(1f),
            isHighlight = stats.achievementsUnlocked > 0
        )
    }
}

@Composable
fun StatCard(
    label: String, 
    value: String, 
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (isHighlight) AccentYellow.copy(alpha = 0.15f) 
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isHighlight) BorderStroke(1.dp, AccentYellow.copy(alpha = 0.5f)) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) DarkGreenPrimary else MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isHighlight) DarkGreenPrimary.copy(alpha = 0.7f) else Color.Gray
            )
        }
    }
}

@Composable
fun CollectionProgressSection(
    progressList: List<CollectionProgress>,
    onCollectionClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Your Collections",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            progressList.forEach { item ->
                CollectionProgressItem(
                    item = item,
                    onClick = { onCollectionClick(item.collection.id) }
                )
            }
        }
    }
}

@Composable
fun CollectionProgressItem(
    item: CollectionProgress,
    onClick: () -> Unit
) {
    val baseColor = Color(android.graphics.Color.parseColor(item.collection.color))
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = baseColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(item.collection.icon, fontSize = 16.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = item.collection.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (item.progress >= 1f) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Mastered",
                        tint = Color(0xFF40916C),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "${(item.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = baseColor
                    )
                }
            }
            
            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (item.progress >= 1f) Color(0xFF40916C) else baseColor,
                trackColor = baseColor.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun AchievementsSection(achievements: List<com.example.brainbites.data.Achievement>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Your Achievements",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        if (achievements.isEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No milestones reached yet. Keep exploring to earn trophies!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                achievements.forEach { achievement ->
                    AchievementCard(achievement = achievement)
                }
            }
        }
    }
}

@Composable
fun ProfileActionSection(
    onEditClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Account Settings",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        ProfileActionItem(title = "Edit Profile", icon = Icons.Default.Edit, onClick = onEditClick)
        ProfileActionItem(title = "Privacy Settings", icon = Icons.Default.Settings, onClick = onPrivacyClick)
    }
}

@Composable
fun ProfileActionItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Your Display Name", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Enter your name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun PrivacySettingsDialog(
    isPublic: Boolean,
    isAnalytics: Boolean,
    onDismiss: () -> Unit,
    onPublicToggle: (Boolean) -> Unit,
    onAnalyticsToggle: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Privacy Settings", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PrivacyToggleItem(
                    title = "Public Profile",
                    description = "Allow others to see your achievements.",
                    checked = isPublic,
                    onCheckedChange = onPublicToggle
                )
                PrivacyToggleItem(
                    title = "Anonymous Analytics",
                    description = "Help us improve BrainBites.",
                    checked = isAnalytics,
                    onCheckedChange = onAnalyticsToggle
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun PrivacyToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ChevronRight is missing in the imports above, adding it via fully qualified name for now or update imports.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    com.example.brainbites.ui.theme.BrainBitesTheme {
        ProfileScreenContent(
            stats = UserStats(
                factsRead = 12, 
                favoritesCount = 5, 
                achievementsUnlocked = 2,
                level = 3,
                rankTitle = "Learner",
                streak = 5
            ),
            achievements = emptyList(),
            userName = "Knowledge Seeker",
            onEditClick = {},
            onPrivacyClick = {},
            onCollectionClick = {}
        )
    }
}
