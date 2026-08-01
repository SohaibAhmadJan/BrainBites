package com.example.brainbites.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PreferenceManager {
    private const val PREFS_NAME = "brain_bites_preferences"
    private const val KEY_DAILY_GOAL = "daily_reading_goal"
    private const val KEY_TEXT_SCALE = "text_scale_multiplier"
    private const val KEY_HAPTICS = "haptic_feedback_enabled"
    private const val KEY_NOTIFIED_ACHIEVEMENTS = "notified_achievements"
    private const val KEY_USER_NAME = "user_display_name"
    private const val KEY_USER_IMAGE = "user_profile_image"
    private const val KEY_USER_BIO = "user_profile_bio"
    private const val KEY_USER_ID = "user_unique_id"
    private const val KEY_PUBLIC_PROFILE = "is_public_profile"
    private const val KEY_ANALYTICS = "is_analytics_enabled"
    private const val KEY_STREAK = "current_streak_count"
    private const val KEY_LAST_ACTIVE = "last_active_timestamp"

    private val _dailyGoal = MutableStateFlow(5)
    val dailyGoal = _dailyGoal.asStateFlow()

    private val _textScale = MutableStateFlow(1.0f)
    val textScale = _textScale.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled = _hapticsEnabled.asStateFlow()

    private val _notifiedAchievements = MutableStateFlow<Set<String>>(emptySet())
    val notifiedAchievements = _notifiedAchievements.asStateFlow()

    private val _userName = MutableStateFlow("Knowledge Seeker")
    val userName = _userName.asStateFlow()

    private val _userImage = MutableStateFlow("")
    val userImage = _userImage.asStateFlow()

    private val _userBio = MutableStateFlow("Curious mind exploring the world of psychology.")
    val userBio = _userBio.asStateFlow()

    private val _userId = MutableStateFlow("")
    val userId = _userId.asStateFlow()

    private val _isPublicProfile = MutableStateFlow(false)
    val isPublicProfile = _isPublicProfile.asStateFlow()

    private val _isAnalyticsEnabled = MutableStateFlow(true)
    val isAnalyticsEnabled = _isAnalyticsEnabled.asStateFlow()

    private val _streakCount = MutableStateFlow(0)
    val streakCount = _streakCount.asStateFlow()

    private val _lastActiveTimestamp = MutableStateFlow(0L)

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _dailyGoal.value = prefs.getInt(KEY_DAILY_GOAL, 5)
        _textScale.value = prefs.getFloat(KEY_TEXT_SCALE, 1.0f)
        _hapticsEnabled.value = prefs.getBoolean(KEY_HAPTICS, true)
        _notifiedAchievements.value = prefs.getStringSet(KEY_NOTIFIED_ACHIEVEMENTS, emptySet()) ?: emptySet()
        _userName.value = prefs.getString(KEY_USER_NAME, "Knowledge Seeker") ?: "Knowledge Seeker"
        _userImage.value = prefs.getString(KEY_USER_IMAGE, "") ?: ""
        _userBio.value = prefs.getString(KEY_USER_BIO, "Curious mind exploring the world of psychology.") ?: "Curious mind exploring the world of psychology."
        
        var savedId = prefs.getString(KEY_USER_ID, "") ?: ""
        if (savedId.isEmpty()) {
            savedId = "user_${java.util.UUID.randomUUID().toString().take(8)}"
            prefs.edit().putString(KEY_USER_ID, savedId).apply()
        }
        _userId.value = savedId

        _isPublicProfile.value = prefs.getBoolean(KEY_PUBLIC_PROFILE, false)
        _isAnalyticsEnabled.value = prefs.getBoolean(KEY_ANALYTICS, true)
        _streakCount.value = prefs.getInt(KEY_STREAK, 0)
        _lastActiveTimestamp.value = prefs.getLong(KEY_LAST_ACTIVE, 0L)
    }

    fun setDailyGoal(context: Context, goal: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_DAILY_GOAL, goal).apply()
        _dailyGoal.value = goal
    }

    fun setTextScale(context: Context, scale: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putFloat(KEY_TEXT_SCALE, scale).apply()
        _textScale.value = scale
    }

    fun setHapticsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_HAPTICS, enabled).apply()
        _hapticsEnabled.value = enabled
    }

    fun setUserName(context: Context, name: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_USER_NAME, name).apply()
        _userName.value = name
    }

    fun setUserImage(context: Context, image: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_USER_IMAGE, image).apply()
        _userImage.value = image
    }

    fun setUserBio(context: Context, bio: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_USER_BIO, bio).apply()
        _userBio.value = bio
    }

    fun setUserId(context: Context, id: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_USER_ID, id).apply()
        _userId.value = id
    }

    fun setPublicProfile(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_PUBLIC_PROFILE, enabled).apply()
        _isPublicProfile.value = enabled
    }

    fun setAnalyticsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ANALYTICS, enabled).apply()
        _isAnalyticsEnabled.value = enabled
    }

    fun updateStreak(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val last = _lastActiveTimestamp.value
        
        val calendar = java.util.Calendar.getInstance()
        
        // Normalize today and last to compare dates
        calendar.timeInMillis = now
        val todayYear = calendar.get(java.util.Calendar.YEAR)
        val todayDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        
        calendar.timeInMillis = last
        val lastYear = calendar.get(java.util.Calendar.YEAR)
        val lastDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)

        if (todayYear == lastYear && todayDay == lastDay) {
            // Already active today, do nothing
            return
        }

        // Check if yesterday
        calendar.timeInMillis = now
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterdayYear = calendar.get(java.util.Calendar.YEAR)
        val yesterdayDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)

        val newStreak = if (lastYear == yesterdayYear && lastDay == yesterdayDay) {
            _streakCount.value + 1
        } else {
            1 // Reset to 1 if more than a day missed
        }

        prefs.edit().putInt(KEY_STREAK, newStreak).putLong(KEY_LAST_ACTIVE, now).apply()
        _streakCount.value = newStreak
        _lastActiveTimestamp.value = now
    }

    fun markAchievementAsNotified(context: Context, id: String) {
        val current = _notifiedAchievements.value.toMutableSet()
        if (current.add(id)) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putStringSet(KEY_NOTIFIED_ACHIEVEMENTS, current).apply()
            _notifiedAchievements.value = current
        }
    }
}
