package com.example.brainbites.ui.categories

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainbites.data.BiteCategory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainbites.ui.components.BrandHeader
import com.example.brainbites.ui.theme.BrainBitesTheme
import kotlinx.coroutines.delay

@Composable
fun CategoryListScreen(
    onCategoryClick: (String) -> Unit,
    viewModel: CategoriesViewModel = viewModel()
) {
    val categoryList by viewModel.categories.collectAsState()
    CategoryListContent(
        categoryList = categoryList,
        onCategoryClick = onCategoryClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListContent(
    categoryList: List<CategoryInfo>,
    onCategoryClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(categoryList) { index, item ->
                val animatedProgress = remember { Animatable(0f) }

                LaunchedEffect(Unit) {
                    delay(index * 40L)
                    animatedProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }

                Box(
                    modifier = Modifier.graphicsLayer {
                        alpha = animatedProgress.value
                        translationY = (1f - animatedProgress.value) * 60.dp.toPx()
                        scaleX = 0.95f + (animatedProgress.value * 0.05f)
                        scaleY = 0.95f + (animatedProgress.value * 0.05f)
                    }
                ) {
                    CategoryGridItem(
                        info = item,
                        onClick = { onCategoryClick(item.category.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryGridItem(info: CategoryInfo, onClick: () -> Unit) {
    val categoryColor = Color(android.graphics.Color.parseColor(info.category.colorHex))
    val softBackground = categoryColor.copy(alpha = 0.1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = info.category.iconRes,
                fontSize = 80.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 20.dp)
                    .alpha(0.1f)
                    .rotate(-15f),
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.surface, softBackground)
                        )
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = categoryColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = info.category.iconRes, fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = info.category.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${info.count} Facts",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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
        CategoryListContent(
            categoryList = sampleCategories,
            onCategoryClick = {}
        )
    }
}
