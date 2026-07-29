package com.example.brainbites.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String, 
    val title: String? = null, 
    val icon: ImageVector? = null,
    val outlinedIcon: ImageVector? = null
) {
    object Splash : Screen("splash_screen")
    
    // Hubs (Parents)
    object HomeHub : Screen("home_hub", "Home", Icons.Default.Home, Icons.Outlined.Home)
    object ExploreHub : Screen("explore_hub", "Explore", Icons.Default.GridView, Icons.Outlined.GridView)
    object SavedHub : Screen("saved_hub", "Saved", Icons.Default.Favorite, Icons.Outlined.FavoriteBorder)
    object SettingsHub : Screen("settings_hub", "Settings", Icons.Default.Settings, Icons.Outlined.Settings)

    // Screens (Children)
    object Home : Screen("home_screen")
    object Categories : Screen("categories_screen")
    object Favorites : Screen("favorites_screen")
    object Settings : Screen("settings_screen")
    object Quiz : Screen("quiz_screen")
    object Teaser : Screen("teaser_screen")
    object History : Screen("history_screen")
    object Profile : Screen("profile_screen")
    object Notifications : Screen("notifications_screen")
    
    // Depth Screens with Hub Prefixes (for persistent highlighting)
    object HomeDetail : Screen("home/detail/{factId}") {
        fun createRoute(factId: String) = "home/detail/$factId"
    }

    object ExploreList : Screen("explore/list/{categoryId}") {
        fun createRoute(categoryId: String) = "explore/list/$categoryId"
    }
    object ExploreDetail : Screen("explore/detail/{factId}") {
        fun createRoute(factId: String) = "explore/detail/$factId"
    }

    object SavedDetail : Screen("saved/detail/{factId}") {
        fun createRoute(factId: String) = "saved/detail/$factId"
    }

    object CollectionDetail : Screen("collection/detail/{collectionId}") {
        fun createRoute(collectionId: String) = "collection/detail/$collectionId"
    }
}

val bottomNavItems = listOf(
    Screen.HomeHub,
    Screen.ExploreHub,
    Screen.SavedHub,
    Screen.SettingsHub
)
