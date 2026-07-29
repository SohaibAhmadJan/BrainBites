package com.example.brainbites.data.theme

import android.content.Context
import com.example.brainbites.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private const val PREFS_NAME = "brain_bites_theme_prefs"
    private const val KEY_THEME = "current_theme"

    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode = _themeMode.asStateFlow()

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedTheme = prefs.getString(KEY_THEME, ThemeMode.LIGHT.name)
        _themeMode.value = try {
            ThemeMode.valueOf(savedTheme ?: ThemeMode.LIGHT.name)
        } catch (e: Exception) {
            ThemeMode.LIGHT
        }
    }

    fun setTheme(context: Context, mode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _themeMode.value = mode
    }
}
