package com.example.brainbites.ui.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainbites.ui.theme.DarkGreenPrimary
import com.example.brainbites.ui.theme.BrainBitesTheme

@Composable
fun QuizScreen(
    onBack: () -> Unit,
    viewModel: QuizViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.isQuizFinished -> {
                QuizResultView(
                    score = uiState.score,
                    total = uiState.questions.size,
                    onRestart = { viewModel.startNewSession() },
                    onDone = onBack
                )
            }
            else -> {
                val currentQuestion = uiState.questions[uiState.currentQuestionIndex]
                QuizQuestionView(
                    questionNumber = uiState.currentQuestionIndex + 1,
                    totalQuestions = uiState.questions.size,
                    question = currentQuestion.quizQuestion ?: "",
                    options = currentQuestion.quizOptions ?: emptyList(),
                    correctIndex = currentQuestion.correctAnswerIndex ?: 0,
                    selectedIndex = uiState.selectedOptionIndex,
                    onOptionSelected = { viewModel.selectOption(it) },
                    onNext = { viewModel.nextQuestion() }
                )
            }
        }
    }
}

@Composable
fun QuizQuestionView(
    questionNumber: Int,
    totalQuestions: Int,
    question: String,
    options: List<String>,
    correctIndex: Int,
    selectedIndex: Int?,
    onOptionSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { questionNumber.toFloat() / totalQuestions },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Question $questionNumber of $totalQuestions",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = question,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEachIndexed { index, option ->
                    val isSelected = selectedIndex == index
                    val isCorrect = index == correctIndex

                    val cardColor = when {
                        selectedIndex == null -> Color.White
                        isCorrect -> Color(0xFFD1E7DD) // Soft Green
                        isSelected && !isCorrect -> Color(0xFFF8D7DA) // Soft Red
                        else -> Color.White
                    }

                    val borderColor = when {
                        selectedIndex == null -> Color(0xFFE0E0E0)
                        isCorrect -> Color(0xFF198754)
                        isSelected && !isCorrect -> Color(0xFFDC3545)
                        else -> Color(0xFFE0E0E0)
                    }

                    Card(
                        onClick = { onOptionSelected(index) },
                        enabled = selectedIndex == null,
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedIndex == null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                            )
                            if (selectedIndex != null) {
                                if (isCorrect) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Spacer for the floating-style arrow button
            Spacer(modifier = Modifier.height(100.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp)
        ) {
            FloatingActionButton(
                onClick = onNext,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun QuizResultView(score: Int, total: Int, onRestart: () -> Unit, onDone: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✨", style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Quiz Completed!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You scored $score out of $total",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Try Another Quiz", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Text("Back to Home", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun QuizQuestionPreview() {
    BrainBitesTheme {
        QuizQuestionView(
            questionNumber = 1,
            totalQuestions = 5,
            question = "What increases the likelihood of someone complying with a request?",
            options = listOf("A sense of urgency", "Providing any reason, even a weak one", "Speaking in a loud tone", "Offering a financial reward"),
            correctIndex = 1,
            selectedIndex = null,
            onOptionSelected = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun QuizResultPreview() {
    BrainBitesTheme {
        QuizResultView(
            score = 4,
            total = 5,
            onRestart = {},
            onDone = {}
        )
    }
}
