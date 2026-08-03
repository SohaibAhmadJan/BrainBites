package com.example.brainbites.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.brainbites.data.NotificationRepository
import com.example.brainbites.data.PreferenceManager
import com.example.brainbites.navigation.Screen
import com.example.brainbites.navigation.bottomNavItems
import com.example.brainbites.ui.components.BrandHeader
import com.example.brainbites.ui.components.LottieBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController = rememberNavController(),
    pagerState: PagerState? = null,
    content: @Composable (Modifier) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val canNavigateBack = navController.previousBackStackEntry != null
    val isRootScreen = currentRoute == "root_pager"
    val showBackButton = canNavigateBack && !isRootScreen

    val currentTitle = "BrainBites"

    Box(modifier = Modifier.fillMaxSize()) {
        LottieBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { BrandHeader(title = currentTitle) },
                    actions = {
                        val unreadCount by NotificationRepository.getUnreadCount().collectAsState(initial = 0)

                        if (currentRoute != Screen.Profile.route) {
                            IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        if (currentRoute != Screen.Notifications.route) {
                            IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                                BadgedBox(
                                    badge = {
                                        if (unreadCount > 0) {
                                            Badge {
                                                Text(unreadCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        actionIconContentColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        ) { innerPadding ->
            val haptics = LocalHapticFeedback.current
            val hapticsEnabled by PreferenceManager.hapticsEnabled.collectAsState()

            Box(modifier = Modifier.fillMaxSize()) {
                // Main Content Area (Full Bleed behind nav bar)
                content(Modifier.padding(top = innerPadding.calculateTopPadding()))

                // Floating Navigation Overlay (Bottom)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 46.dp)
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val tabWidth = this.maxWidth / 4
                        
                        val currentIndex = when {
                            currentRoute == "root_pager" -> pagerState?.currentPage ?: 0
                            currentRoute?.startsWith("home") == true -> 0
                            currentRoute?.startsWith("explore") == true -> 1
                            currentRoute?.startsWith("saved") == true -> 2
                            currentRoute?.startsWith("settings") == true -> 3
                            else -> pagerState?.currentPage ?: 0
                        }

                        val indicatorOffset by animateDpAsState(
                            targetValue = tabWidth * currentIndex,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                            label = "indicatorOffset"
                        )

                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            shadowElevation = 16.dp,
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(72.dp)) {
                                // 1. Sliding Indicator Pill
                                Surface(
                                    modifier = Modifier
                                        .offset(x = indicatorOffset + (tabWidth - 64.dp) / 2)
                                        .align(Alignment.CenterStart)
                                        .size(width = 64.dp, height = 40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {}

                                // 2. Navigation Items
                                Row(modifier = Modifier.fillMaxSize()) {
                                    bottomNavItems.forEachIndexed { index, screen ->
                                        val selected = currentIndex == index
                                        
                                        val scale by animateFloatAsState(
                                            targetValue = if (selected) 1.15f else 1f,
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                            label = "iconScale"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    if (hapticsEnabled) {
                                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    }
                                                    if (currentRoute == "root_pager") {
                                                        coroutineScope.launch {
                                                            pagerState?.scrollToPage(index)
                                                        }
                                                    } else {
                                                        navController.popBackStack("root_pager", inclusive = false)
                                                        coroutineScope.launch {
                                                            pagerState?.scrollToPage(index)
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val iconToUse = if (selected) screen.icon else screen.outlinedIcon ?: screen.icon
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                iconToUse?.let {
                                                    Icon(
                                                        imageVector = it,
                                                        contentDescription = screen.title,
                                                        tint = if (selected) MaterialTheme.colorScheme.primary 
                                                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .graphicsLayer(scaleX = scale, scaleY = scale)
                                                    )
                                                }
                                                
                                                AnimatedVisibility(
                                                    visible = selected,
                                                    enter = fadeIn() + slideInVertically { it / 2 },
                                                    exit = fadeOut() + slideOutVertically { it / 2 }
                                                ) {
                                                    Text(
                                                        text = screen.title ?: "",
                                                        color = MaterialTheme.colorScheme.primary,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(top = 2.dp),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Global Back Button Overlay (Below TopAppBar, Top-Left)
                AnimatedVisibility(
                    visible = showBackButton,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f),
                    modifier = Modifier
                        .padding(top = innerPadding.calculateTopPadding() + 8.dp)
                        .padding(start = 16.dp)
                        .align(Alignment.TopStart)
                ) {
                    SmallFloatingActionButton(
                        onClick = { 
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

}
