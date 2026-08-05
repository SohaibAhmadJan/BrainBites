package com.example.brainbites.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.brainbites.ui.components.AvatarPicker
import com.example.brainbites.ui.components.AvatarView
import com.example.brainbites.ui.theme.AccentYellow
import com.example.brainbites.ui.theme.DarkGreenPrimary
import com.example.brainbites.ui.theme.GreenSecondary
import com.example.brainbites.ui.util.getCollectionIcon
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun ProfileScreen(
    onCollectionClick: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userBio by viewModel.userBio.collectAsState()
    val userId by viewModel.userId.collectAsState()
    val userImage by viewModel.userImage.collectAsState()
    val isPublic by viewModel.isPublicProfile.collectAsState()
    val isAnalytics by viewModel.isAnalyticsEnabled.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = userName,
            currentBio = userBio,
            currentId = userId,
            currentImage = userImage,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, bio, id, image ->
                viewModel.updateProfile(name, bio, id, image)
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
        userBio = userBio,
        userId = userId,
        userImage = userImage,
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
    userBio: String,
    userId: String,
    userImage: String,
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
                    userId = userId,
                    userImage = userImage,
                    level = stats.level,
                    rankTitle = stats.rankTitle,
                    streak = stats.streak
                )
            }
        }

        // 1.5 Bio Section (NEW)
        item {
            AnimatedEntrance(index = 1) {
                BioSection(bio = userBio)
            }
        }

        // 2. Stats Row
        item {
            AnimatedEntrance(index = 2) {
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
        
        item { Spacer(modifier = Modifier.height(150.dp)) }
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    userId: String,
    userImage: String,
    level: Int,
    rankTitle: String,
    streak: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AvatarView(
            userName = userName,
            userImage = userImage,
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "@$userId",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Level $level • $rankTitle",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (streak > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
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
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BioSection(bio: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "About Me",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    label: String, 
    value: String, 
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val icon = getCollectionIcon(item.collection.id)
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
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
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "${(item.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (item.progress >= 1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                achievements.forEach { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        modifier = Modifier.fillMaxWidth()
                    )
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
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
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
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentBio: String,
    currentId: String,
    currentImage: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var bio by remember { mutableStateOf(currentBio) }
    var userId by remember { mutableStateOf(currentId) }
    var userImage by remember { mutableStateOf(currentImage) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { userImage = it.toString() }
        }
    )

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedLabelColor = MaterialTheme.colorScheme.primary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                AvatarPicker(
                    selectedAvatar = userImage,
                    onAvatarSelected = { userImage = it },
                    onGalleryClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Display Name", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Enter your name") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = textFieldColors
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("User ID", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = userId,
                        onValueChange = { userId = it.replace(" ", "_").lowercase() },
                        placeholder = { Text("unique_id") },
                        leadingIcon = { Text("@", modifier = Modifier.padding(start = 12.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = textFieldColors
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Bio", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        placeholder = { Text("Tell us about yourself...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = textFieldColors
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, bio, userId, userImage) },
                enabled = name.isNotBlank() && userId.isNotBlank()
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
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedBorderColor = MaterialTheme.colorScheme.primary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    uncheckedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        }
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
            userBio = "Curious mind exploring the world of psychology.",
            userId = "knowledge_seeker",
            userImage = "🧠",
            onEditClick = {},
            onPrivacyClick = {},
            onCollectionClick = {}
        )
    }
}
