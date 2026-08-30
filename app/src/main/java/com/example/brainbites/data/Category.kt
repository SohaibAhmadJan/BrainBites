package com.example.brainbites.data

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val icon: String = "🧠",
    val vectorIcon: String = "Brain",
    val color: String = "#2D6A4F",
    val description: String = "",
    val sortOrder: Int = 0,
    val isPublished: Boolean = true
) {
    // Helper to convert to a display-ready object if needed
    fun getDisplayName() = name
}
