package com.example.brainbites.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import android.net.Uri

@Composable
fun AvatarView(
    userName: String,
    userImage: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    indicatorSize: Boolean = false // If true, optimizes for small top bar icon
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        val initial = userName.firstOrNull()?.toString()?.uppercase() ?: "?"
        
        val imageData: Any? = when {
            userImage.startsWith("content://") || userImage.startsWith("file://") -> {
                try {
                    Uri.parse(userImage)
                } catch (e: Exception) {
                    userImage
                }
            }
            userImage.isNotEmpty() -> {
                "https://api.dicebear.com/9.x/personas/png?seed=$userImage"
            }
            else -> null
        }

        if (imageData != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageData)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                loading = {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(if (indicatorSize) 16.dp else 32.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = if (indicatorSize) 2.dp else 4.dp
                        )
                    }
                },
                error = {
                    InitialAvatarView(initial = initial, indicatorSize = indicatorSize)
                }
            )
        } else {
            InitialAvatarView(initial = initial, indicatorSize = indicatorSize)
        }
    }
}

@Composable
private fun InitialAvatarView(initial: String, indicatorSize: Boolean) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initial,
                style = if (indicatorSize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.displayMedium,
                fontWeight = if (indicatorSize) FontWeight.Bold else FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
