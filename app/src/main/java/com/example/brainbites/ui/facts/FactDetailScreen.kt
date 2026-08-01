package com.example.brainbites.ui.facts

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.brainbites.data.BiteCategory
import com.example.brainbites.data.BiteItem
import com.example.brainbites.data.BiteRepository
import com.example.brainbites.ui.components.QuoteCard
import com.example.brainbites.ui.components.shimmer
import com.example.brainbites.ui.theme.BrainBitesTheme
import com.example.brainbites.ui.util.ShareUtils
import com.example.brainbites.ui.util.captureComposable
import com.example.brainbites.ui.util.getIconDrawable
import com.example.brainbites.ui.util.rememberComposableCaptureController
import kotlinx.coroutines.launch

@Composable
fun FactDetailScreen(
    initialFactId: String,
    onBack: () -> Unit,
    viewModel: FactListViewModel = viewModel()
) {
    val context = LocalContext.current
    val facts by viewModel.facts.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val captureController = rememberComposableCaptureController()
    
    LaunchedEffect(Unit) {
        viewModel.loadFacts("ALL")
    }
    
    LaunchedEffect(initialFactId) {
        BiteRepository.addToHistory(context, initialFactId)
    }

    val selectedFact = remember(facts, initialFactId) {
        facts.find { it.id == initialFactId }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Off-screen QuoteCard for capturing
        if (selectedFact != null) {
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
                QuoteCard(fact = selectedFact)
            }
        }

        FactDetailContent(
            fact = selectedFact,
            onBack = onBack,
            onToggleBookmark = { id -> viewModel.toggleBookmark(id) },
            onShare = { fact ->
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
fun FactDetailContent(
    fact: BiteItem?,
    onBack: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    onShare: (BiteItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (fact == null) {
            Box(Modifier.padding(top = 40.dp).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                FactPage(
                    fact = fact,
                    onToggleBookmark = { onToggleBookmark(fact.id) },
                    onShare = { onShare(fact) }
                )
            }
        }
    }
}

@Composable
fun FactPage(fact: BiteItem, onToggleBookmark: () -> Unit, onShare: () -> Unit) {
    val context = LocalContext.current
    val categoryColor = Color(android.graphics.Color.parseColor(fact.category.colorHex))
    
    Box(modifier = Modifier.fillMaxSize()) {
        // --- BACKGROUND LAYER ---
        // 1. Dynamic Category Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            categoryColor.copy(alpha = 0.15f)
                        )
                    )
                )
        )

        // 2. Ghost Emojis Pattern (Kept as requested)
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = fact.category.iconRes,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-20).dp, y = 40.dp)
                    .alpha(0.08f)
                    .graphicsLayer {
                        rotationZ = -15f
                        scaleX = 1.5f
                        scaleY = 1.5f
                    }
            )
            Text(
                text = fact.category.iconRes,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 40.dp, y = 100.dp)
                    .alpha(0.06f)
                    .graphicsLayer {
                        rotationZ = 20f
                        scaleX = 2.25f
                        scaleY = 2.25f
                    }
            )
            Text(
                text = fact.category.iconRes,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-10).dp, y = 20.dp)
                    .alpha(0.07f)
                    .graphicsLayer {
                        rotationZ = -10f
                        scaleX = 1.75f
                        scaleY = 1.75f
                    }
            )
        }

        // --- CONTENT LAYER ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
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
                border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.3f))
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
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                // Fallback to Vector Icon
                                Icon(
                                    painter = painterResource(id = fact.category.getIconDrawable()),
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = categoryColor
                                )
                            }
                        }
                    )

                    // Sublayer: Gradient for label legibility
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

                    // Visual Anchoring: Category Label Overlay
                    Surface(
                        color = categoryColor,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = fact.category.displayName.uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
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
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val clipboardManager = LocalClipboardManager.current
                
                // Save Button
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
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = if (fact.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (fact.isBookmarked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (fact.isBookmarked) "Saved" else "Save", style = MaterialTheme.typography.labelMedium)
                }

                // Share Button
                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface, 
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", style = MaterialTheme.typography.labelMedium)
                }

                // Copy Button
                Button(
                    onClick = { 
                        clipboardManager.setText(AnnotatedString(fact.fact))
                        Toast.makeText(context, "Fact copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface, 
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", style = MaterialTheme.typography.labelMedium)
                }
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
            onToggleBookmark = { _ -> },
            onShare = { _ -> }
        )
    }
}
