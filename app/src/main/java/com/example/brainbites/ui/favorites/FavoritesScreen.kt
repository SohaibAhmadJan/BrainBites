package com.example.brainbites.ui.favorites

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainbites.data.BiteCategory
import com.example.brainbites.data.BiteItem
import com.example.brainbites.data.BiteRepository
import com.example.brainbites.ui.components.BiteCard
import com.example.brainbites.ui.theme.BrainBitesTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val _favorites = MutableStateFlow<List<BiteItem>>(emptyList())
    val favorites = _favorites.asStateFlow()

    init {
        viewModelScope.launch {
            BiteRepository.getFavoriteFacts(getApplication()).collect {
                _favorites.value = it
            }
        }
    }

    fun toggleBookmark(id: String) {
        viewModelScope.launch {
            BiteRepository.toggleBookmark(getApplication(), id)
        }
    }
}

@Composable
fun FavoritesScreen(
    onFactClick: (String) -> Unit,
    viewModel: FavoritesViewModel = viewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    FavoritesContent(
        favorites = favorites,
        onFactClick = onFactClick,
        onToggleBookmark = { id -> viewModel.toggleBookmark(id) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesContent(
    favorites: List<BiteItem>,
    onFactClick: (String) -> Unit,
    onToggleBookmark: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (favorites.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❤️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No favorites yet", color = MaterialTheme.colorScheme.secondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favorites, key = { it.id }) { fact ->
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavoritesScreenPreview() {
    BrainBitesTheme {
        val sampleFact = BiteItem(
            id = "1",
            fact = "Humans tend to mimic the body language of people they're comfortable with.",
            category = BiteCategory.HUMAN_BEHAVIOR,
            isBookmarked = true
        )
        FavoritesContent(
            favorites = listOf(sampleFact),
            onFactClick = {},
            onToggleBookmark = { _ -> }
        )
    }
}
