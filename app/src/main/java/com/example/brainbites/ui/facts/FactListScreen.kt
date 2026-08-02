package com.example.brainbites.ui.facts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainbites.data.BiteCategory
import com.example.brainbites.data.BiteItem
import com.example.brainbites.ui.components.BiteCard
import com.example.brainbites.ui.components.BrandHeader
import com.example.brainbites.ui.theme.BrainBitesTheme

@Composable
fun FactListScreen(
    categoryId: String,
    onFactClick: (String) -> Unit,
    viewModel: FactListViewModel = viewModel()
) {
    val facts by viewModel.facts.collectAsState()
    
    LaunchedEffect(categoryId) {
        viewModel.loadFacts(categoryId)
    }

    FactListContent(
        categoryId = categoryId,
        facts = facts,
        onFactClick = onFactClick,
        onToggleBookmark = { id -> viewModel.toggleBookmark(id) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactListContent(
    categoryId: String,
    facts: List<BiteItem>,
    onFactClick: (String) -> Unit,
    onToggleBookmark: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 150.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(facts, key = { it.id }) { fact ->
                BiteCard(
                    bite = fact,
                    onToggleBookmark = { id -> onToggleBookmark(id) },
                    onFactClick = onFactClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FactListScreenPreview() {
    BrainBitesTheme {
        val sampleFact = BiteItem(
            id = "1",
            fact = "Humans tend to mimic the body language of people they're comfortable with.",
            category = BiteCategory.HUMAN_BEHAVIOR
        )
        FactListContent(
            categoryId = "HUMAN_BEHAVIOR",
            facts = listOf(sampleFact),
            onFactClick = {},
            onToggleBookmark = { _ -> }
        )
    }
}
