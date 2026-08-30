package com.example.brainbites.data

import kotlinx.serialization.Serializable

@Serializable
data class QuoteItem(
    val id: String = "",
    val text: String = "",
    val author: String = "",
    val category: String = "Human Behavior",
    val isActive: Boolean = true,
    val createdAt: String = ""
)
