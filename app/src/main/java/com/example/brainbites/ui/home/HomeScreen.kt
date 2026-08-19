package com.example.brainbites.ui.home

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainbites.data.Achievement
import com.example.brainbites.data.BiteCategory
import com.example.brainbites.data.BiteItem
import com.example.brainbites.ui.home.components.DailyTipCard
import com.example.brainbites.ui.components.AnimatedEntrance
import com.example.brainbites.ui.components.*
import com.example.brainbites.ui.theme.*
import com.example.brainbites.ui.util.ShareUtils
import com.example.brainbites.ui.util.captureComposable
import com.example.brainbites.ui.util.getIconDrawable
import com.example.brainbites.ui.util.premiumClickable
import com.example.brainbites.ui.util.rememberComposableCaptureController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToCategory: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToTeaser: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val allFacts by viewModel.allFacts.collectAsState()
    val rotatingFact by viewModel.rotatingFact.collectAsState()
    val recentlyViewed by viewModel.recentlyViewed.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()
    val moodMessage by viewModel.moodMessage.collectAsState()
    val dailyTip by viewModel.dailyTip.collectAsState()
    val settings by com.example.brainbites.data.SettingsRepository.settings.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    val captureController = rememberComposableCaptureController()
    val context = LocalContext.current

    val currentRotatingFact = rotatingFact

    Box(modifier = Modifier.fillMaxSize()) {
        // Off-screen QuoteCard for capturing
        if (currentRotatingFact != null) {
            Box(
                modifier = Modifier
                    .size(1080.dp)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            placeable.place(-2000, -2000) // Positioned far off-screen
                        }
                    }
                    .captureComposable(captureController)
            ) {
                QuoteCard(fact = currentRotatingFact)
            }
        }

        HomeScreenContent(
            allFacts = allFacts,
            factOfTheDay = currentRotatingFact,
            recentlyViewed = recentlyViewed,
            achievements = achievements,
            selectedMood = selectedMood,
            moodMessage = moodMessage,
            dailyTip = dailyTip,
            sectionsOrder = settings.homeSectionsOrder,
            onMoodSelected = { viewModel.selectMood(it) },
            onNavigateToCategory = onNavigateToCategory,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToQuiz = onNavigateToQuiz,
            onNavigateToTeaser = onNavigateToTeaser,
            onNavigateToHistory = onNavigateToHistory,
            onToggleBookmark = { id -> viewModel.toggleBookmark(id) },
            onShareFact = { fact ->
                coroutineScope.launch {
                    val bitmap = captureController.captureToBitmap()
                    ShareUtils.shareFactAsImage(context, bitmap, fact.fact)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    allFacts: List<BiteItem>,
    factOfTheDay: BiteItem?,
    recentlyViewed: List<BiteItem>,
    achievements: List<Achievement>,
    selectedMood: String?,
    moodMessage: String?,
    dailyTip: PsychologyTip?,
    sectionsOrder: List<String>,
    onMoodSelected: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToTeaser: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    onShareFact: (BiteItem) -> Unit
) {
    var isContentReady by remember { mutableStateOf(false) }

    LaunchedEffect(allFacts) {
        if (allFacts.isNotEmpty()) {
            delay(500)
            isContentReady = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = isContentReady, animationSpec = tween(600), label = "homeContent") { ready ->
            if (ready) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 150.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    sectionsOrder.forEachIndexed { index, section ->
                        when (section) {
                            "HERO" -> item {
                                AnimatedEntrance(index = index) {
                                    factOfTheDay?.let { fact ->
                                        FactOfTheDayCard(
                                            fact = fact,
                                            onToggleBookmark = { onToggleBookmark(fact.id) },
                                            onShare = { onShareFact(fact) },
                                            onClick = { onNavigateToDetail(fact.id) }
                                        )
                                    }
                                }
                            }
                            "CATEGORIES" -> item {
                                AnimatedEntrance(index = index) {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(
                                            text = "Explore Categories",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontWeight = FontWeight.Bold
                                        )
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            items(BiteCategory.entries.filter { it != BiteCategory.ALL }) { category ->
                                                CategoryChip(
                                                    category = category,
                                                    isSelected = false,
                                                    onSelect = { onNavigateToCategory(it.name) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            "QUICK_ACTIONS" -> item {
                                AnimatedEntrance(index = index) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        QuickActionCard(
                                            title = "Quiz Mode",
                                            description = "Test your knowledge",
                                            icon = Icons.Default.Extension,
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            onClick = onNavigateToQuiz,
                                            modifier = Modifier.weight(1f)
                                        )
                                        QuickActionCard(
                                            title = "Daily Teaser",
                                            description = "Quick mental puzzle",
                                            icon = Icons.Default.Lightbulb,
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            onClick = onNavigateToTeaser,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                            "MOOD" -> item {
                                AnimatedEntrance(index = index) {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        DailyMoodSection(selectedMood = selectedMood, onMoodSelected = onMoodSelected)
                                        AnimatedContent(targetState = moodMessage, label = "moodMsg") { msg ->
                                            if (msg != null) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                                ) {
                                                    Text(text = msg, modifier = Modifier.padding(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            "RECENT" -> if (recentlyViewed.isNotEmpty()) item {
                                AnimatedEntrance(index = index) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Recently Viewed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        recentlyViewed.forEach { fact ->
                                            BiteCard(bite = fact, onToggleBookmark = onToggleBookmark, onFactClick = onNavigateToDetail)
                                        }
                                    }
                                }
                            }
                            "DISCOVER" -> item {
                                AnimatedEntrance(index = index) {
                                    Button(
                                        onClick = { allFacts.randomOrNull()?.let { onNavigateToDetail(it.id) } },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Discover Something New") }
                                }
                            }
                            "ACHIEVEMENTS" -> item {
                                AnimatedEntrance(index = index) {
                                    AchievementsSection(achievements = achievements)
                                }
                            }
                            "TIP" -> item {
                                dailyTip?.let { tip ->
                                    AnimatedEntrance(index = index) {
                                        DailyTipCard(tip = tip)
                                    }
                                }
                            }
                            "TRENDING" -> item {
                                AnimatedEntrance(index = index) {
                                    TrendingQuotesSection(allFacts = allFacts, onNavigateToDetail = onNavigateToDetail)
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            } else {
                HomeScreenShimmer(PaddingValues(0.dp))
            }
        }
    }
}

@Composable
fun DailyMoodSection(selectedMood: String?, onMoodSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "How are you feeling today?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val moods = listOf("😊 Happy", "😌 Calm", "😔 Sad", "😤 Stressed", "💡 Motivated")
            items(moods) { mood ->
                val isSelected = selectedMood == mood
                Surface(
                    modifier = Modifier
                        .premiumClickable(
                            glowColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primaryContainer,
                            onClick = { onMoodSelected(mood) }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = mood,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementsSection(achievements: List<Achievement>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Achievements",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Track your learning journey and unlock milestones.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        val pagerState = rememberPagerState { achievements.size }
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = 16.dp
        ) { page ->
            AchievementCard(
                achievement = achievements[page],
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TrendingQuotesSection(allFacts: List<BiteItem>, onNavigateToDetail: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Trending Quotes",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        val trendingFacts = allFacts.take(4)
        trendingFacts.chunked(2).forEach { rowFacts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowFacts.forEach { fact ->
                    TrendingQuoteCard(
                        fact = fact,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToDetail(fact.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun FactOfTheDayCard(fact: BiteItem, onToggleBookmark: (String) -> Unit, onShare: () -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(24.dp))
            .premiumClickable(
                glowColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header: Category badge (Static) & Actions (Static)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(160.dp)
                ) {
                    // Animated text only inside static category card
                    AnimatedContent(
                        targetState = fact.category,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) togetherWith
                                    fadeOut(animationSpec = tween(400))
                        },
                        label = "categoryTextRotation",
                        modifier = Modifier.fillMaxWidth()
                    ) { category ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                                Icon(
                                    painter = painterResource(id = category.getIconDrawable()),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = category.displayName.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center
                                )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = {
                            onToggleBookmark(fact.id)
                            val message = if (fact.isBookmarked) "Removed from favorites" else "Added to favorites"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (fact.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (fact.isBookmarked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

                // Main Content: Quote text only (Animated)
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = fact.fact,
                        transitionSpec = {
                            (slideInVertically { height -> height / 2 } + fadeIn(tween(600))).togetherWith(
                                slideOutVertically { height -> -height / 2 } + fadeOut(tween(600))
                            )
                        },
                        label = "quoteTextRotation",
                        modifier = Modifier.fillMaxSize()
                    ) { factText ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = factText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, // Matching "Test Your Knowledge"
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    }
                }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer: Read More & Bite Label (Static)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Read More",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(110.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "BITE OF THE DAY",
                            modifier = Modifier.padding(vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenShimmer(padding: PaddingValues) {
    Column(modifier = Modifier.padding(padding).padding(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(24.dp)).shimmer())
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.width(150.dp).height(24.dp).shimmer())
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) {
                Box(modifier = Modifier.size(width = 100.dp, height = 40.dp).clip(RoundedCornerShape(20.dp)).shimmer())
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    BrainBitesTheme {
        val sampleFact = BiteItem(
            id = "1",
            fact = "Humans tend to mimic the body language of people they're comfortable with.",
            category = BiteCategory.HUMAN_BEHAVIOR,
            title = "The Chameleon Effect"
        )
        HomeScreenContent(
            allFacts = listOf(sampleFact),
            factOfTheDay = sampleFact,
            recentlyViewed = listOf(sampleFact),
            achievements = emptyList(),
            selectedMood = null,
            moodMessage = null,
            dailyTip = PsychologyTip("Sample Tip", "This is a preview message."),
            sectionsOrder = listOf("HERO", "CATEGORIES", "QUICK_ACTIONS", "MOOD", "RECENT", "DISCOVER", "ACHIEVEMENTS", "TIP", "TRENDING"),
            onMoodSelected = {},
            onNavigateToCategory = {},
            onNavigateToDetail = {},
            onNavigateToQuiz = {},
            onNavigateToTeaser = {},
            onNavigateToHistory = {},
            onToggleBookmark = { _ -> },
            onShareFact = { _ -> }
        )
    }
}
