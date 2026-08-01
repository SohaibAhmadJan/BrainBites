package com.example.brainbites.ui.collections

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainbites.data.BiteItem
import com.example.brainbites.data.BiteRepository
import com.example.brainbites.data.CollectionSet
import com.example.brainbites.ui.components.BiteCard
import com.example.brainbites.ui.theme.BrainBitesTheme
import com.example.brainbites.ui.util.getCollectionIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CollectionDetailViewModel(private val repository: BiteRepository, private val collectionId: String) : ViewModel() {
    private val _collection = MutableStateFlow<CollectionSet?>(null)
    val collection: StateFlow<CollectionSet?> = _collection.asStateFlow()

    private val _facts = MutableStateFlow<List<BiteItem>>(emptyList())
    val facts: StateFlow<List<BiteItem>> = _facts.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _collection.value = repository.getCollection(collectionId)
        
        // We use a scope here because repository flows are long-lived
        // In a real app, we'd use viewModelScope.launch and collect
        // For simplicity in this implementation:
    }
    
    fun observeData(context: android.content.Context, scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            repository.getFactsForCollection(collectionId).collect {
                _facts.value = it
            }
        }
        scope.launch {
            repository.getCollectionProgress(collectionId).collect {
                _progress.value = it
            }
        }
    }

    fun toggleBookmark(context: android.content.Context, id: String) {
        scope.launch {
            repository.toggleBookmark(context, id)
        }
    }
    
    private val scope = kotlinx.coroutines.MainScope()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: String,
    onBack: () -> Unit,
    onFactClick: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Simple ViewModel factory
    val viewModel: CollectionDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CollectionDetailViewModel(BiteRepository, collectionId) as T
            }
        }
    )

    val collection by viewModel.collection.collectAsState()
    val facts by viewModel.facts.collectAsState()
    val progress by viewModel.progress.collectAsState()

    LaunchedEffect(collectionId) {
        viewModel.observeData(context, scope)
    }

    collection?.let { set ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 80.dp, 16.dp, 16.dp), // Added top padding to avoid overlap
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header section
                item {
                    CollectionHeader(set, progress)
                }

                // Fact list
                items(facts) { fact ->
                    BiteCard(
                        bite = fact,
                        onToggleBookmark = { id -> viewModel.toggleBookmark(context, id) },
                        onFactClick = onFactClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item { Spacer(modifier = Modifier.height(112.dp)) }
            }
        }
    }
}

@Composable
fun CollectionHeader(
    collection: CollectionSet,
    progress: Float
) {
    val icon = getCollectionIcon(collection.id)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = collection.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (progress >= 1f) "Collection Mastered!" else "Progress",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }

            if (progress >= 1f) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "You've unlocked all insights in this set.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
