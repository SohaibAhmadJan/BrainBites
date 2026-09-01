package com.example.brainbites

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.brainbites.data.AnalyticsRepository
import com.example.brainbites.data.PreferenceManager
import com.example.brainbites.data.SettingsRepository
import com.example.brainbites.data.AuthRepository
import com.example.brainbites.data.BiteRepository
import com.example.brainbites.data.NotificationRepository
import com.example.brainbites.data.AchievementRepository
import com.example.brainbites.data.AutomationManager
import com.example.brainbites.data.theme.ThemeManager
import com.example.brainbites.navigation.BrainBitesNavGraph
import com.example.brainbites.ui.theme.BrainBitesTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private var initialFactId by mutableStateOf<String?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.e("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("BRAIN_BITES", "MainActivity: onCreate TRIGGERED")
        android.widget.Toast.makeText(this, "BrainBites Starting...", android.widget.Toast.LENGTH_SHORT).show()
        super.onCreate(savedInstanceState)
        
        checkNotificationPermission()
        ThemeManager.initialize(this)
        PreferenceManager.initialize(this)
        AnalyticsRepository.initializeInstallation(this)
        SettingsRepository.startListening()
        AutomationManager.initialize(this, lifecycleScope)
        NotificationRepository.startGlobalListener(this) // Start heartbeat immediately
        AnalyticsRepository.logAppOpen()

        lifecycleScope.launch {
            AuthRepository.syncUser(this@MainActivity)
            AuthRepository.updateLastActive()
        }

        // Parallel Launch: Sync user data and personal notifications
        lifecycleScope.launch {
            AuthRepository.currentUser.collect { user ->
                user?.let { 
                    PreferenceManager.syncWithServer(it)
                    NotificationRepository.startUserListener(this@MainActivity, it.account.uid)
                    AchievementRepository.syncUserAchievements(it.account.uid)
                    
                    // Trigger token sync and topic subscription
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            lifecycleScope.launch {
                                AuthRepository.syncDeviceToken(this@MainActivity, task.result)
                            }
                        }
                    }
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("global_broadcasts")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("MainActivity", "Subscribed to global_broadcasts topic")
                            } else {
                                Log.e("MainActivity", "Topic subscription failed")
                            }
                        }
                }
            }
        }

        lifecycleScope.launch {
            BiteRepository.initializeDatabase(this@MainActivity)
            AchievementRepository.fetchDefinitions()
        }
        handleIntent(intent)
        enableEdgeToEdge()
        
        setContent {
            val themeMode by ThemeManager.themeMode.collectAsState()
            val textScale by PreferenceManager.textScale.collectAsState()
            val isDisabled by AuthRepository.isAccountDisabled.collectAsState()
            val settings by SettingsRepository.settings.collectAsState()
            
            BrainBitesTheme(
                themeMode = themeMode,
                textScale = textScale
            ) {
                if (settings.maintenanceMode) {
                    com.example.brainbites.ui.main.MaintenanceScreen(
                        message = settings.maintenanceMessage
                    )
                } else if (isDisabled) {
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

    private fun checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
