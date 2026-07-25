package com.example.brainbites.ui.teaser

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainbites.ui.theme.DarkGreenPrimary

@Composable
fun DailyTeaserScreen(
    onBack: () -> Unit,
    viewModel: TeaserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            uiState.teaserFact?.let { fact ->
                TeaserContent(
                    fact = fact.fact,
                    question = fact.quizQuestion ?: "",
                    isRevealed = uiState.isRevealed,
                    onReveal = { viewModel.reveal() },
                    onDone = onBack
                )
            }
        }
    }
}

@Composable
fun TeaserContent(
    fact: String,
    question: String,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            tint = Color(0xFFE9C46A),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Mystery Insight",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreenPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                AnimatedContent(
                    targetState = isRevealed,
                    transitionSpec = {
                        fadeIn(tween(500)) togetherWith fadeOut(tween(500))
                    },
                    label = "revealAnimation"
                ) { revealed ->
                    if (revealed) {
                        Text(
                            text = fact,
                            style = MaterialTheme.typography.bodyLarge,
                            color = DarkGreenPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    } else {
                        Button(
                            onClick = onReveal,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reveal Insight", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        if (isRevealed) {
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary)
            ) {
                Text("Got it!", fontWeight = FontWeight.Bold)
            }
        }
    }
}
