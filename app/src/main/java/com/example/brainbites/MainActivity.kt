package com.example.brainbites

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.brainbites.data.theme.ThemeManager
import com.example.brainbites.navigation.BrainBitesNavGraph
import com.example.brainbites.ui.theme.BrainBitesTheme
import com.example.brainbites.ui.util.TaglineManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ThemeManager.initialize(this)
        TaglineManager.start(lifecycleScope)
        enableEdgeToEdge()
        
        setContent {
            val themeMode by ThemeManager.themeMode.collectAsState()
            
            BrainBitesTheme(themeMode = themeMode) {
                BrainBitesNavGraph()
            }
        }
    }
}
