package com.example.brainbites.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * A curated set of seeds for DiceBear Personas to provide diverse, professional illustrations.
 */
val AVATAR_SEEDS = listOf(
    "Felix", "Aneka", "Jack", "Avery", "Willow",
    "Luna", "George", "Oliver", "Jasper", "Leo",
    "Maya", "Zoe", "Sasha", "Finn", "Toby",
    "Max", "Milo", "Jade", "Ruby", "Coco",
    "Bella", "Beau", "Charlie", "Daisy", "Echo",
    "Faye", "Gus", "Hazel", "Iris", "Jude",
    "Kai", "Lola", "Nico", "Olive", "Piper",
    "Quinn", "Remy", "Skye", "Theo", "Vera"
)

val AVATAR_KEYS = listOf("GALLERY", "INITIALS") + AVATAR_SEEDS

@Composable
fun AvatarPicker(
    selectedAvatar: String,
    onAvatarSelected: (String) -> Unit,
    onGalleryClick: () -> Unit
) {
    val context = LocalContext.current
    val effectiveSelected = when {
        selectedAvatar.isEmpty() -> "INITIALS"
        selectedAvatar.startsWith("content://") || selectedAvatar.startsWith("file://") -> "GALLERY"
        else -> selectedAvatar
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Select Profile Style",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(AVATAR_KEYS) { key ->
                val isSelected = key == effectiveSelected
                
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .clickable { 
                            when (key) {
                                "GALLERY" -> onGalleryClick()
                                "INITIALS" -> onAvatarSelected("")
                                else -> onAvatarSelected(key)
                            }
                        },
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        when (key) {
                            "GALLERY" -> {
                                if (selectedAvatar.startsWith("content://") || selectedAvatar.startsWith("file://")) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(selectedAvatar)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Gallery",
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            "INITIALS" -> {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "Aa",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            else -> {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data("https://api.dicebear.com/9.x/personas/png?seed=$key")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    error = {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = key.take(1),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
