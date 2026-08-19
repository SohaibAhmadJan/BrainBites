package com.example.brainbites.navigation

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.brainbites.ui.collections.CollectionDetailScreen
import com.example.brainbites.ui.theme.SoftBackground
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.launch

@Composable
fun BrainBitesNavGraph(
    navController: NavHostController = rememberNavController(),
    initialFactId: String? = null
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
                    if (initialFactId != null) {
                        navController.navigate(Screen.ExploreDetail.createRoute(initialFactId))
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
    val coroutineScope = rememberCoroutineScope()
    
    val pagerState = rememberPagerState { 4 }
    
    // Sync Bottom Nav with Pager
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Map Pager index to Hub routes
    val hubs = listOf(
        Screen.HomeHub.route,
        Screen.ExploreHub.route,
        Screen.SavedHub.route,
        Screen.SettingsHub.route
    )

    // Directional slide mapping (still useful for non-pager screens)
    val routeToIndex = mapOf(
        Screen.HomeHub.route to 0,
        Screen.Home.route to 0,
        Screen.Quiz.route to 0,
        Screen.Teaser.route to 0,
        Screen.History.route to 0,
        Screen.HomeDetail.route to 0,
        
        Screen.ExploreHub.route to 1,
        Screen.Categories.route to 1,
        Screen.ExploreList.route to 1,
        Screen.ExploreDetail.route to 1,
        
        Screen.SavedHub.route to 2,
        Screen.Favorites.route to 2,
        Screen.SavedDetail.route to 2,
        
        Screen.SettingsHub.route to 3,
        Screen.Settings.route to 3,
        
        Screen.CollectionDetail.route to 1,
        
        Screen.Profile.route to 4,
        Screen.Notifications.route to 4
    )

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        MainScaffold(
            navController = nestedNavController,
            pagerState = pagerState // Passing pagerState to scaffold
        ) { modifier ->
            Surface(modifier = modifier, color = Color.Transparent) {
                NavHost(
                    navController = nestedNavController,
                    startDestination = "root_pager", // The pager is now the home
                    enterTransition = {
                        val initialRoute = initialState.destination.route ?: ""
                        val targetRoute = targetState.destination.route ?: ""
                        
                        val initialIndex = routeToIndex[initialRoute] ?: 0
                        val targetIndex = routeToIndex[targetRoute] ?: 0

                        if (targetIndex > initialIndex) {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(400)
                            ) + fadeIn(animationSpec = tween(400))
                        } else {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(400)
                            ) + fadeIn(animationSpec = tween(400))
                        }
                    },
                    exitTransition = {
                        val initialRoute = initialState.destination.route ?: ""
                        val targetRoute = targetState.destination.route ?: ""
                        
                        val initialIndex = routeToIndex[initialRoute] ?: 0
                        val targetIndex = routeToIndex[targetRoute] ?: 0

                        if (targetIndex > initialIndex) {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(400)
                            ) + fadeOut(animationSpec = tween(400))
                        } else {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(400)
                            ) + fadeOut(animationSpec = tween(400))
                        }
                    }
                ) {
                    composable("root_pager") {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { index ->
                            when (index) {
                                0 -> HomeScreen(
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
                                1 -> CategoryListScreen(
                                    onCategoryClick = { id -> 
                                        nestedNavController.navigate(Screen.ExploreList.createRoute(id))
                                    },
                                    onCollectionClick = { id ->
                                        nestedNavController.navigate(Screen.CollectionDetail.createRoute(id))
                                    },
                                    onFactClick = { id ->
                                        nestedNavController.navigate(Screen.ExploreDetail.createRoute(id))
                                    }
                                )
                                2 -> FavoritesScreen(
                                    onFactClick = { id ->
                                        nestedNavController.navigate(Screen.SavedDetail.createRoute(id))
                                    }
                                )
                                3 -> SettingsScreen()
                            }
                        }
                    }

                    // Deep Screens
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
                    
                    // Explore Deep Screens
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

                    // Saved Deep Screens
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

                    composable(
                        route = Screen.CollectionDetail.route,
                        arguments = listOf(navArgument("collectionId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
                        CollectionDetailScreen(
                            collectionId = collectionId,
                            onBack = { nestedNavController.popBackStack() },
                            onFactClick = { factId: String ->
                                nestedNavController.navigate(Screen.ExploreDetail.createRoute(factId))
                            }
                        )
                    }

                    composable(route = Screen.Profile.route) {
                        ProfileScreen(
                            onCollectionClick = { id ->
                                nestedNavController.navigate(Screen.CollectionDetail.createRoute(id))
                            }
                        )
                    }

                    composable(route = Screen.Notifications.route) {
                        NotificationsScreen()
                    }
                }
            }
        }
    }
}
