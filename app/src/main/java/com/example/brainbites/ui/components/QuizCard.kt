package com.example.brainbites.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.brainbites.data.BiteItem
import com.example.brainbites.ui.theme.AccentYellow
import com.example.brainbites.ui.theme.DarkGreenPrimary

@Composable
fun QuizCard(
    bite: BiteItem,
    onAnsweredCorrectly: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quizQuestion = bite.quizQuestion ?: return
    val quizOptions = bite.quizOptions ?: return
    val correctAnswerIndex = bite.correctAnswerIndex ?: return

    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFC)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = DarkGreenPrimary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "QUIZ CHALLENGE",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = quizQuestion,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(14.dp))

            quizOptions.forEachIndexed { index, option ->
                val isSelected = selectedOption == index
                val isCorrect = index == correctAnswerIndex

                val backgroundColor by animateColorAsState(
                    targetValue = when {
                        isSubmitted && isCorrect -> Color(0xFFD1E7DD)
                        isSubmitted && isSelected && !isCorrect -> Color(0xFFF8D7DA)
                        isSelected -> AccentYellow.copy(alpha = 0.3f)
                        else -> Color.White
                    },
                    label = "bgColor"
                )

                val borderColor by animateColorAsState(
                    targetValue = when {
                        isSubmitted && isCorrect -> Color(0xFF198754)
                        isSubmitted && isSelected && !isCorrect -> Color(0xFFDC3545)
                        isSelected -> DarkGreenPrimary
                        else -> Color(0xFFE0E0E0)
                    },
                    label = "borderColor"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(backgroundColor)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .clickable(enabled = !isSubmitted) {
                            selectedOption = index
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = DarkGreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isSubmitted) {
                Button(
                    onClick = {
                        if (selectedOption != null) {
                            isSubmitted = true
                            if (selectedOption == correctAnswerIndex) {
                                onAnsweredCorrectly()
                            }
                        }
                    },
                    enabled = selectedOption != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreenPrimary,
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Submit Answer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                val isSuccess = selectedOption == correctAnswerIndex
                Text(
                    text = if (isSuccess) "🎉 Correct!" else "❌ Incorrect. Correct answer: ${quizOptions[correctAnswerIndex]}",
                    color = if (isSuccess) Color(0xFF198754) else Color(0xFFDC3545),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
