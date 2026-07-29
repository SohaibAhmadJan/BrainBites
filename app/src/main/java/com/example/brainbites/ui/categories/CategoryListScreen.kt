package com.example.brainbites.ui.categories

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.example.brainbites.ui.components.AnimatedEntrance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brainbites.data.BiteCategory
import com.example.brainbites.data.BiteItem
import com.example.brainbites.data.BiteRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainbites.ui.theme.BrainBitesTheme
import com.example.brainbites.ui.util.getIconDrawable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import kotlinx.coroutines.delay

@Composable
fun CategoryListScreen(
    onCategoryClick: (String) -> Unit,
    onCollectionClick: (String) -> Unit,
    onFactClick: (String) -> Unit,
    viewModel: CategoriesViewModel = viewModel()
) {
    val categoryList by viewModel.categories.collectAsState()
    val collectionList by viewModel.collections.collectAsState()
    val featuredFacts by viewModel.featuredFacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    ExploreScreenContent(
        categoryList = categoryList,
        collectionList = collectionList,
        featuredFacts = featuredFacts,
        searchQuery = searchQuery,
        searchResults = searchResults,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onCategoryClick = onCategoryClick,
        onCollectionClick = onCollectionClick,
        onFactClick = onFactClick,
        onSurpriseMe = { 
            viewModel.getRandomFactId()?.let { onFactClick(it) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreenContent(
    categoryList: List<CategoryInfo>,
    collectionList: List<com.example.brainbites.data.CollectionSet>,
    featuredFacts: List<BiteItem>,
    searchQuery: String,
    searchResults: List<BiteItem>,
    onSearchQueryChange: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onCollectionClick: (String) -> Unit,
    onFactClick: (String) -> Unit,
    onSurpriseMe: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = { 
                    Text(
                        text = "Search facts, topics, categories...",
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                singleLine = true
            )
        }

        if (searchQuery.isNotEmpty()) {
            if (searchResults.isEmpty()) {
                item {
                    NoResultsState(
                        query = searchQuery,
                        onSurpriseMe = onSurpriseMe
                    )
                }
            } else {
                // Search Results
                items(searchResults) { fact ->
                    Text(
                        text = fact.fact,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFactClick(fact.id) }
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                }
            }
        } else {
            // 2. Magic Discovery (Surprise Me)
            item {
                AnimatedEntrance(index = 1) {
                    SurpriseMeCard(onClick = onSurpriseMe)
                }
            }

            // 3. Featured Insights
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Featured Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(featuredFacts) { index, fact ->
                            AnimatedEntrance(index = index + 2, delayMultiplier = 80L) {
                                FeaturedFactCard(fact = fact, onClick = { onFactClick(fact.id) })
                            }
                        }
                    }
                }
            }

            // 3.5 Learning Collections (NEW)
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Learning Collections",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(collectionList) { index, set ->
                            AnimatedEntrance(index = index + 3, delayMultiplier = 100L) {
                                CollectionCard(
                                    collection = set,
                                    onClick = { onCollectionClick(set.id) }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Categories Grid Header
            item {
                Text(
                    text = "Browse by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            // 5. Categories Grid (Using chunks to simulate grid in LazyColumn)
            val chunks = categoryList.chunked(2)
            chunks.forEachIndexed { rowIndex, rowItems ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowItems.forEachIndexed { colIndex, item ->
                            val globalIndex = rowIndex * 2 + colIndex
                            Box(modifier = Modifier.weight(1f)) {
                                AnimatedEntrance(index = globalIndex + 5) {
                                    CategoryGridItem(
                                        info = item,
                                        onClick = { onCategoryClick(item.category.name) }
                                    )
                                }
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurpriseMeCard(onClick: () -> Unit) {
    val startColor = Color(0xFF8E2DE2) // Magic Purple
    val endColor = Color(0xFF4A00E0)   // Electric Blue

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .heightIn(min = 100.dp),
        shape = RoundedCornerShape(24.dp),
        color = endColor // Root solid color to prevent gaps
    ) {
        // Background applied to root container within Surface
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(colors = listOf(startColor, endColor))
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center // Content centering
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Surprise Me",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Discover a random psychological insight",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedFactCard(fact: BiteItem, onClick: () -> Unit) {
    val categoryColor = Color(android.graphics.Color.parseColor(fact.category.colorHex))
    val endColor = categoryColor.copy(alpha = 0.12f)
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(260.dp)
            .heightIn(min = 140.dp),
        shape = RoundedCornerShape(20.dp),
        color = endColor,
        border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            categoryColor.copy(alpha = 0.22f),
                            endColor
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = fact.fact,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    color = categoryColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = fact.category.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryGridItem(info: CategoryInfo, onClick: () -> Unit) {
    val categoryColor = Color(android.graphics.Color.parseColor(info.category.colorHex))
    val endColor = categoryColor.copy(alpha = 0.15f)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp),
        shape = RoundedCornerShape(24.dp),
        color = endColor,
        border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            categoryColor.copy(alpha = 0.05f),
                            endColor
                        )
                    )
                ),
            contentAlignment = Alignment.Center // ROOT CENTERING
        ) {
            //decorative ghost icon strictly in background
            Text(
                text = info.category.iconRes,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 20.dp)
                    .alpha(0.1f)
                    .rotate(-15f),
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = categoryColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Icon perfectly centered within square
                        Icon(
                            painter = painterResource(id = info.category.getIconDrawable()),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = categoryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = info.category.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${info.count} Facts",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NoResultsState(
    query: String,
    onSurpriseMe: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No results for \"$query\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Try searching for simpler terms like \"brain\", \"habit\", or \"love\".",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedButton(
            onClick = onSurpriseMe,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Surprise Me")
        }
    }
}

@Composable
fun CollectionCard(
    collection: com.example.brainbites.data.CollectionSet,
    onClick: () -> Unit
) {
    val baseColor = Color(android.graphics.Color.parseColor(collection.color))
    val context = LocalContext.current
    val progress by BiteRepository.getCollectionProgress(collection.id).collectAsState(initial = 0f)
    val readCount = (progress * collection.factIds.size).toInt()
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(220.dp)
            .height(160.dp), // Increased height for progress bar
        shape = RoundedCornerShape(24.dp),
        color = baseColor.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, baseColor.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Icon
            Text(
                text = collection.icon,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 10.dp)
                    .alpha(0.15f)
                    .rotate(-20f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = baseColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = collection.icon,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    if (progress >= 1f) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Mastered",
                            tint = Color(0xFF40916C),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = collection.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$readCount/${collection.factIds.size} Insights",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = baseColor,
                            fontWeight = FontWeight.Black
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (progress >= 1f) Color(0xFF40916C) else baseColor,
                        trackColor = baseColor.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CategoryListScreenPreview() {
    BrainBitesTheme {
        val sampleCategories = listOf(
            CategoryInfo(BiteCategory.HUMAN_BEHAVIOR, 15),
            CategoryInfo(BiteCategory.MENTAL_HEALTH, 12),
            CategoryInfo(BiteCategory.BRAIN_SCIENCE, 10)
        )
        ExploreScreenContent(
            categoryList = sampleCategories,
            collectionList = emptyList(),
            featuredFacts = emptyList(),
            searchQuery = "",
            searchResults = emptyList(),
            onSearchQueryChange = {},
            onCategoryClick = {},
            onCollectionClick = {},
            onFactClick = {},
            onSurpriseMe = {}
        )
    }
}
