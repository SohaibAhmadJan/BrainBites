package com.example.brainbites

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.brainbites.data.PreferenceManager
import com.example.brainbites.data.theme.ThemeManager
import com.example.brainbites.navigation.BrainBitesNavGraph
import com.example.brainbites.ui.theme.BrainBitesTheme
import com.example.brainbites.ui.util.TaglineManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ThemeManager.initialize(this)
        PreferenceManager.initialize(this)
        enableEdgeToEdge()
        
        setContent {
            val themeMode by ThemeManager.themeMode.collectAsState()
            val textScale by PreferenceManager.textScale.collectAsState()
            
            BrainBitesTheme(
                themeMode = themeMode,
                textScale = textScale
            ) {
                BrainBitesNavGraph()
            }
        }
    }
}
