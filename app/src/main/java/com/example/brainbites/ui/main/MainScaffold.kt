package com.example.brainbites.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.brainbites.navigation.Screen
import com.example.brainbites.navigation.bottomNavItems
import com.example.brainbites.ui.components.BrandHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController = rememberNavController(),
    content: @Composable (Modifier) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val canNavigateBack = navController.previousBackStackEntry != null
    val isRootScreen = currentRoute in listOf(
        Screen.Home.route,
        Screen.Categories.route,
        Screen.Favorites.route,
        Screen.Settings.route
    )

    val currentTitle = when {
        currentRoute == Screen.Home.route -> "BrainBites"
        currentRoute == Screen.Categories.route -> "Explore"
        currentRoute == Screen.Favorites.route -> "Saved Facts"
        currentRoute == Screen.Settings.route -> "Settings"
        currentRoute == Screen.History.route -> "Recently Viewed"
        currentRoute == Screen.Quiz.route -> "Psychology Quiz"
        currentRoute == Screen.Teaser.route -> "Daily Teaser"
        currentRoute?.startsWith("explore/list") == true -> "Facts"
        currentRoute?.contains("/detail/") == true -> "Facts"
        else -> "BrainBites"
    }

    val isDetailScreen = currentRoute?.contains("/detail/") == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { BrandHeader(title = currentTitle) },
                actions = {
                    if (isDetailScreen) {
                        IconButton(onClick = { /* Global Share placeholder - Logic typically remains in screen or shared VM */ }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        IconButton(onClick = { /* Profile Placeholder */ }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { /* Notification Placeholder */ }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    
                    NavigationBarItem(
                        icon = { 
                            screen.icon?.let { 
                                Icon(
                                    imageVector = it, 
                                    contentDescription = screen.title
                                ) 
                            } 
                        },
                        label = { 
                            Text(
                                text = screen.title ?: "",
                                fontSize = 10.sp
                            ) 
                        },
                        selected = selected,
                        onClick = {
                            val isReselection = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                            if (isReselection) {
                                navController.popBackStack(screen.route, inclusive = false)
                            } else {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = screen.route != Screen.HomeHub.route && 
                                                  screen.route != Screen.ExploreHub.route && 
                                                  screen.route != Screen.SavedHub.route
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content(Modifier)
            
            if (canNavigateBack && !isRootScreen) {
                SmallFloatingActionButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }
    }
}
