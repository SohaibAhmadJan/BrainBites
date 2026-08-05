package com.example.brainbites.ui.notifications

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainbites.data.Notification
import com.example.brainbites.data.NotificationType
import com.example.brainbites.ui.theme.DarkGreenPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    NotificationsScreenContent(
        notifications = notifications,
        onMarkAsRead = { viewModel.markAsRead(it) },
        onMarkAllAsRead = { viewModel.markAllAsRead() },
        onClearAll = { viewModel.clearAll() }
    )
}

@Composable
fun NotificationsScreenContent(
    notifications: List<Notification>,
    onMarkAsRead: (String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onClearAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Actions Bar
        if (notifications.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onMarkAllAsRead) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Read All", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = onClearAll) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear All", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        if (notifications.isEmpty()) {
            EmptyNotifications()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 150.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = { onMarkAsRead(notification.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (notification.isRead) Color.Transparent 
                      else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
        label = "bgChange"
    )

    Surface(
        onClick = onClick,
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Type Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getNotificationColor(notification.type).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = getNotificationColor(notification.type),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        color = if (notification.isRead) MaterialTheme.colorScheme.onSurface 
                                else MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatTimestamp(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyNotifications() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Notifications Yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Stay tuned for updates and insights.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

fun getNotificationIcon(type: NotificationType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        NotificationType.NEW_FACT -> Icons.Default.Lightbulb
        NotificationType.ACHIEVEMENT -> Icons.Default.EmojiEvents
        NotificationType.SYSTEM -> Icons.Default.Info
        NotificationType.GENERAL -> Icons.Default.Notifications
    }
}

@Composable
fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.NEW_FACT -> MaterialTheme.colorScheme.error
        NotificationType.ACHIEVEMENT -> Color(0xFFF4A261) // Golden
        NotificationType.SYSTEM -> MaterialTheme.colorScheme.primary
        NotificationType.GENERAL -> MaterialTheme.colorScheme.secondary
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    com.example.brainbites.ui.theme.BrainBitesTheme {
        NotificationsScreenContent(
            notifications = listOf(
                Notification("1", "Daily Insight", "The power of habit is stronger than you think.", System.currentTimeMillis(), false, NotificationType.NEW_FACT),
                Notification("2", "Achievement", "You've read 5 facts!", System.currentTimeMillis() - 10000, true, NotificationType.ACHIEVEMENT)
            ),
            onMarkAsRead = {},
            onMarkAllAsRead = {},
            onClearAll = {}
        )
    }
}
