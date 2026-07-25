package com.example.brainbites.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Splash Screen.
 * Manages the splash timing and navigation state.
 */
class SplashViewModel : ViewModel() {

    private val _isSplashFinished = MutableStateFlow(false)
    val isSplashFinished = _isSplashFinished.asStateFlow()

    // Timer logic removed: Navigation is now controlled by the UI animation sequence 
    // to ensure a smooth fade-out before switching screens.
}
