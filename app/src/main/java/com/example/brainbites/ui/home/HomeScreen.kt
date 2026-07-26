package com.example.brainbites.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainbites.data.BiteCategory
import com.example.brainbites.data.BiteItem
import com.example.brainbites.ui.components.*
import com.example.brainbites.ui.theme.*
import com.example.brainbites.ui.util.ShareUtils
import com.example.brainbites.ui.util.getIconDrawable
import android.widget.Toast
import kotlinx.coroutines.delay

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

    HomeScreenContent(
        allFacts = allFacts,
        factOfTheDay = rotatingFact,
        recentlyViewed = recentlyViewed,
        onNavigateToCategory = onNavigateToCategory,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToQuiz = onNavigateToQuiz,
        onNavigateToTeaser = onNavigateToTeaser,
        onNavigateToHistory = onNavigateToHistory,
        onToggleBookmark = { id -> viewModel.toggleBookmark(id) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    allFacts: List<BiteItem>,
    factOfTheDay: BiteItem?,
    recentlyViewed: List<BiteItem>,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToTeaser: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onToggleBookmark: (String) -> Unit
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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. HERO CARD: Fact of the Day
                    item {
                        factOfTheDay?.let { fact ->
                            FactOfTheDayCard(
                                fact = fact,
                                onToggleBookmark = { onToggleBookmark(fact.id) },
                                onClick = { onNavigateToDetail(fact.id) }
                            )
                        }
                    }

                    // 2. Categories Header & Row
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Explore Categories",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
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

                    // 3. New Quick Actions (Quiz & Teaser)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            QuickActionCard(
                                title = "Quiz Mode",
                                description = "Test your knowledge",
                                icon = Icons.Default.Extension,
                                containerColor = Color(0xFFA8DADC),
                                contentColor = Color(0xFF1B4332),
                                onClick = onNavigateToQuiz,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionCard(
                                title = "Daily Teaser",
                                description = "Quick mental puzzle",
                                icon = Icons.Default.Lightbulb,
                                containerColor = Color(0xFFFFE8D6),
                                contentColor = Color(0xFFE76F51),
                                onClick = onNavigateToTeaser,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 4. Recently Viewed Section
                    if (recentlyViewed.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recently Viewed",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = onNavigateToHistory) {
                                    Text("Show all", color = GreenSecondary, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                recentlyViewed.forEach { fact ->
                                    BiteCard(
                                        bite = fact,
                                        onToggleBookmark = { id -> onToggleBookmark(id) },
                                        onFactClick = onNavigateToDetail,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    // 5. Next Fact Quick Action (Discover Something New)
                    item {
                        Button(
                            onClick = {
                                val randomId = allFacts.randomOrNull()?.id ?: ""
                                if (randomId.isNotEmpty()) onNavigateToDetail(randomId)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Discover Something New", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
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
fun FactOfTheDayCard(fact: BiteItem, onToggleBookmark: (String) -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
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
                    color = AccentYellow,
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
                                tint = DarkGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category.displayName.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = DarkGreenPrimary,
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
                            tint = if (fact.isBookmarked) Color.Red else AccentYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { ShareUtils.shareFact(context, fact.fact) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = AccentYellow,
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
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 26.sp,
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
                        color = AccentYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = AccentYellow,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Surface(
                    color = AccentYellow,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(110.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "BITE OF THE DAY",
                            modifier = Modifier.padding(vertical = 4.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = DarkGreenPrimary
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
            onNavigateToCategory = {},
            onNavigateToDetail = {},
            onNavigateToQuiz = {},
            onNavigateToTeaser = {},
            onNavigateToHistory = {},
            onToggleBookmark = { _ -> }
        )
    }
}
