package com.example.brainbites.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbites.data.BiteItem
import com.example.brainbites.data.BiteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizUiState(
    val questions: List<BiteItem> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val score: Int = 0,
    val isQuizFinished: Boolean = false,
    val isLoading: Boolean = true
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState = _uiState.asStateFlow()

    init {
        startNewSession()
    }

    fun startNewSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isQuizFinished = false, currentQuestionIndex = 0, score = 0, selectedOptionIndex = null) }
            BiteRepository.getAllFacts(getApplication()).collect { facts ->
                if (facts.isNotEmpty()) {
                    val quizQuestions = facts.filter { it.quizQuestion != null }.shuffled().take(5)
                    _uiState.update { it.copy(questions = quizQuestions, isLoading = false) }
                }
            }
        }
    }

    fun selectOption(index: Int) {
        if (_uiState.value.selectedOptionIndex != null) return
        
        val currentQuestion = _uiState.value.questions[_uiState.value.currentQuestionIndex]
        val isCorrect = index == currentQuestion.correctAnswerIndex
        
        _uiState.update { 
            it.copy(
                selectedOptionIndex = index,
                score = if (isCorrect) it.score + 1 else it.score
            )
        }
    }

    fun nextQuestion() {
        val nextIndex = _uiState.value.currentQuestionIndex + 1
        if (nextIndex < _uiState.value.questions.size) {
            _uiState.update { it.copy(currentQuestionIndex = nextIndex, selectedOptionIndex = null) }
        } else {
            _uiState.update { it.copy(isQuizFinished = true) }
        }
    }
}
