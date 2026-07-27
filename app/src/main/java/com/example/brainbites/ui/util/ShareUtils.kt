package com.example.brainbites.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri

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

    fun shareApp(context: Context) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Download BrainBites to learn fascinating psychology facts every day! 🧠 \n\nhttps://play.google.com/store/apps/details?id=com.example.brainbites")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share BrainBites")
        context.startActivity(shareIntent)
    }

    fun rateApp(context: Context) {
        val packageName = context.packageName
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=$packageName")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback for emulators or devices without Play Store
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            }
            context.startActivity(webIntent)
        }
    }
}
