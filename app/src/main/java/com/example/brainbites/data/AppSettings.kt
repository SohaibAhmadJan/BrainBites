package com.example.brainbites.data

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "",
    val latestVersion: String = "3.4.8.7",
    val minVersion: String = "1.0.0",
    val supportEmail: String = "support@brainbites.com",
    val quizzesEnabled: Boolean = true,
    val achievementsEnabled: Boolean = true,
    val dailyFactEnabled: Boolean = true,
    val featuredFactId: String = "1",
    val dailyTipTitle: String = "The 2-Minute Rule",
    val dailyTipMessage: String = "If a task takes less than 2 minutes, do it now.",
    val homeSectionsOrder: List<String> = listOf("HERO", "CATEGORIES", "QUICK_ACTIONS", "MOOD", "RECENT", "DISCOVER", "ACHIEVEMENTS", "TIP", "TRENDING")
)
