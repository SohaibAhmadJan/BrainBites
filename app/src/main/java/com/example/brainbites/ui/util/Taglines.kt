package com.example.brainbites.ui.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object TaglineManager {
    val list = listOf(
        "Feed Your Curiosity.",
        "Bite-Sized Knowledge.",
        "Learn Something New Every Day.",
        "Tiny Facts, Big Ideas.",
        "Knowledge in Every Scroll.",
        "Discover the Unexpected.",
        "Expand Your Mind.",
        "One Fact at a Time.",
        "Discover. Learn. Grow.",
        "Stay Curious.",
        "Learn Beyond the Ordinary.",
        "Knowledge Awaits."
    )

    private val _currentTagline = MutableStateFlow(list.random())
    val currentTagline = _currentTagline.asStateFlow()

    private val _jumpTrigger = MutableStateFlow(0)
    val jumpTrigger = _jumpTrigger.asStateFlow()

    fun refreshTagline() {
        _currentTagline.value = list.filter { it != _currentTagline.value }.randomOrNull() ?: list.random()
    }

    fun triggerJump() {
        _jumpTrigger.value++
    }
}
