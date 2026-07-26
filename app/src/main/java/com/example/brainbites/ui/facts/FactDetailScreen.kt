package com.example.brainbites.ui.facts

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.brainbites.data.BiteCategory
import com.example.brainbites.data.BiteItem
import com.example.brainbites.data.BiteRepository
import com.example.brainbites.ui.components.shimmer
import com.example.brainbites.ui.theme.BrainBitesTheme

@Composable
fun FactDetailScreen(
    initialFactId: String,
    onBack: () -> Unit,
    viewModel: FactListViewModel = viewModel()
) {
    val context = LocalContext.current
    val facts by viewModel.facts.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadFacts("ALL")
    }
    
    LaunchedEffect(initialFactId) {
        BiteRepository.addToHistory(context, initialFactId)
    }

    val selectedFact = remember(facts, initialFactId) {
        facts.find { it.id == initialFactId }
    }

    FactDetailContent(
        fact = selectedFact,
        onBack = onBack,
        onToggleBookmark = { id -> viewModel.toggleBookmark(id) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactDetailContent(
    fact: BiteItem?,
    onBack: () -> Unit,
    onToggleBookmark: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Content Area (Back button handled by MainScaffold)
        if (fact == null) {
            Box(Modifier.padding(top = 40.dp).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                FactPage(
                    fact = fact, 
                    onToggleBookmark = { onToggleBookmark(fact.id) }
                )
            }
        }
    }
}

@Composable
fun FactPage(fact: BiteItem, onToggleBookmark: () -> Unit) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // High-Quality Image Card with Contextual Overlay
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(24.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(fact.imageUrl)
                        .crossfade(600)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(modifier = Modifier.fillMaxSize().shimmer())
                    },
                    error = {
                        // High-End Fallback
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = fact.category.iconRes, fontSize = 64.sp)
                        }
                    }
                )

                // Sublayer: Subtle Gradient for label legibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                                startY = 400f
                            )
                        )
                )

                // Visual Anchoring: Category Label Overlay (Premium Context)
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = fact.category.displayName.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = fact.fact,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    onToggleBookmark()
                    val message = if (fact.isBookmarked) "Removed from favorites" else "Added to favorites"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface, 
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = if (fact.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (fact.isBookmarked) Color.Red else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (fact.isBookmarked) "Saved" else "Save")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FactDetailScreenPreview() {
    BrainBitesTheme {
        val sampleFact = BiteItem(
            id = "1",
            fact = "Humans tend to mimic the body language of people they're comfortable with.",
            category = BiteCategory.HUMAN_BEHAVIOR
        )
        FactDetailContent(
            fact = sampleFact,
            onBack = {},
            onToggleBookmark = { _ -> }
        )
    }
}
