package com.example.brainbites.data

import kotlinx.serialization.Serializable

@Serializable
data class QuizQuestion(
    val id: String = "",
    val factId: String,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val teaserType: String = "MYTH_BUSTER",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class UserQuizResult(
    val factId: String,
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
