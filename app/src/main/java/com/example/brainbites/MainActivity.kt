package com.example.brainbites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.brainbites.data.PreferenceManager
import com.example.brainbites.data.SettingsRepository
import com.example.brainbites.data.AuthRepository
import com.example.brainbites.data.BiteRepository
import com.example.brainbites.data.NotificationRepository
import com.example.brainbites.data.AchievementRepository
import com.example.brainbites.data.theme.ThemeManager
import com.example.brainbites.navigation.BrainBitesNavGraph
import com.example.brainbites.ui.theme.BrainBitesTheme
import com.example.brainbites.ui.util.TaglineManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var initialFactId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ThemeManager.initialize(this)
        PreferenceManager.initialize(this)
        SettingsRepository.startListening()
        
        lifecycleScope.launch {
            AuthRepository.signInAnonymously()
            AuthRepository.updateLastActive()
            BiteRepository.initializeDatabase(this@MainActivity)
            AchievementRepository.fetchDefinitions()
            
            AuthRepository.currentUser.collect { user ->
                user?.let { 
                    PreferenceManager.syncWithServer(it)
                    NotificationRepository.startListening()
                    AchievementRepository.syncUserAchievements(it.account.uid)
                    
                    // Trigger token sync
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            lifecycleScope.launch {
                                AuthRepository.syncDeviceToken(this@MainActivity, task.result)
                            }
                        }
                    }
                }
            }
        }
        handleIntent(intent)
        enableEdgeToEdge()
        
        setContent {
            val themeMode by ThemeManager.themeMode.collectAsState()
            val textScale by PreferenceManager.textScale.collectAsState()
            val isDisabled by AuthRepository.isAccountDisabled.collectAsState()
            
            BrainBitesTheme(
                themeMode = themeMode,
                textScale = textScale
            ) {
                if (isDisabled) {
                    com.example.brainbites.ui.main.AccountDisabledScreen()
                } else {
                    BrainBitesNavGraph(initialFactId = initialFactId)
                }
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        val factId = intent?.getStringExtra("factId")
        if (factId != null) {
            initialFactId = factId
            Log.d("MainActivity", "Launched with deep link for Fact: $factId")
        }
    }
}
