package com.example.brainbites.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.navigation
import com.example.brainbites.ui.home.HomeScreen
import com.example.brainbites.ui.splash.SplashScreen
import com.example.brainbites.ui.categories.CategoryListScreen
import com.example.brainbites.ui.facts.FactListScreen
import com.example.brainbites.ui.facts.FactDetailScreen
import com.example.brainbites.ui.favorites.FavoritesScreen
import com.example.brainbites.ui.settings.SettingsScreen
import com.example.brainbites.ui.quiz.QuizScreen
import com.example.brainbites.ui.teaser.DailyTeaserScreen
import com.example.brainbites.ui.history.HistoryScreen
import com.example.brainbites.ui.profile.ProfileScreen
import com.example.brainbites.ui.notifications.NotificationsScreen
import com.example.brainbites.ui.main.MainScaffold
import com.example.brainbites.ui.theme.SoftBackground
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme

@Composable
fun BrainBitesNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(animationSpec = tween(400)) },
        exitTransition = { fadeOut(animationSpec = tween(400)) }
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("main_root") {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = "main_root") {
            MainContent()
        }
    }
}

@Composable
fun MainContent() {
    val nestedNavController = rememberNavController()
    
    MainScaffold(navController = nestedNavController) { modifier ->
        Surface(modifier = modifier, color = MaterialTheme.colorScheme.background) {
            NavHost(
                navController = nestedNavController,
                startDestination = Screen.HomeHub.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                // --- HOME HUB ---
                navigation(
                    startDestination = Screen.Home.route,
                    route = Screen.HomeHub.route
                ) {
                    composable(route = Screen.Home.route) {
                        HomeScreen(
                            onNavigateToCategory = { id -> 
                                nestedNavController.navigate(Screen.ExploreList.createRoute(id))
                            },
                            onNavigateToDetail = { id -> 
                                nestedNavController.navigate(Screen.HomeDetail.createRoute(id))
                            },
                            onNavigateToQuiz = {
                                nestedNavController.navigate(Screen.Quiz.route)
                            },
                            onNavigateToTeaser = {
                                nestedNavController.navigate(Screen.Teaser.route)
                            },
                            onNavigateToHistory = {
                                nestedNavController.navigate(Screen.History.route)
                            }
                        )
                    }
                    composable(route = Screen.Quiz.route) {
                        QuizScreen(onBack = { nestedNavController.popBackStack() })
                    }
                    composable(route = Screen.Teaser.route) {
                        DailyTeaserScreen(onBack = { nestedNavController.popBackStack() })
                    }
                    composable(route = Screen.History.route) {
                        HistoryScreen(
                            onFactClick = { id ->
                                nestedNavController.navigate(Screen.HomeDetail.createRoute(id))
                            }
                        )
                    }
                    composable(
                        route = Screen.HomeDetail.route,
                        arguments = listOf(navArgument("factId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val factId = backStackEntry.arguments?.getString("factId") ?: ""
                        FactDetailScreen(
                            initialFactId = factId,
                            onBack = { nestedNavController.popBackStack() }
                        )
                    }
                }

                // --- EXPLORE HUB ---
                navigation(
                    startDestination = Screen.Categories.route,
                    route = Screen.ExploreHub.route
                ) {
                    composable(route = Screen.Categories.route) {
                        CategoryListScreen(
                            onCategoryClick = { id -> 
                                nestedNavController.navigate(Screen.ExploreList.createRoute(id))
                            }
                        )
                    }
                    composable(
                        route = Screen.ExploreList.route,
                        arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val categoryId = backStackEntry.arguments?.getString("categoryId") ?: "ALL"
                        FactListScreen(
                            categoryId = categoryId,
                            onFactClick = { id -> 
                                nestedNavController.navigate(Screen.ExploreDetail.createRoute(id))
                            }
                        )
                    }
                    composable(
                        route = Screen.ExploreDetail.route,
                        arguments = listOf(navArgument("factId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val factId = backStackEntry.arguments?.getString("factId") ?: ""
                        FactDetailScreen(
                            initialFactId = factId,
                            onBack = { nestedNavController.popBackStack() }
                        )
                    }
                }

                // --- SAVED HUB ---
                navigation(
                    startDestination = Screen.Favorites.route,
                    route = Screen.SavedHub.route
                ) {
                    composable(route = Screen.Favorites.route) {
                        FavoritesScreen(
                            onFactClick = { id ->
                                nestedNavController.navigate(Screen.SavedDetail.createRoute(id))
                            }
                        )
                    }
                    composable(
                        route = Screen.SavedDetail.route,
                        arguments = listOf(navArgument("factId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val factId = backStackEntry.arguments?.getString("factId") ?: ""
                        FactDetailScreen(
                            initialFactId = factId,
                            onBack = { nestedNavController.popBackStack() }
                        )
                    }
                }

                // --- SETTINGS HUB ---
                navigation(
                    startDestination = Screen.Settings.route,
                    route = Screen.SettingsHub.route
                ) {
                    composable(route = Screen.Settings.route) {
                        SettingsScreen()
                    }
                }

                composable(route = Screen.Profile.route) {
                    ProfileScreen()
                }

                composable(route = Screen.Notifications.route) {
                    NotificationsScreen()
                }
            }
        }
    }
}
