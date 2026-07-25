package com.example.brainbites.ui.util

import android.content.Context
import android.content.Intent

object ShareUtils {
    fun shareFact(context: Context, factText: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Did you know? \n\n$factText\n\nShared from BrainBites 🧠")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share this Psychology Fact")
        context.startActivity(shareIntent)
    }
}
