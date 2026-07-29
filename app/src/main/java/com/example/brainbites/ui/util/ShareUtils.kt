package com.example.brainbites.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.brainbites.data.BiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ShareUtils {
    private const val TAG = "SmartSharing"

    fun shareFact(context: Context, factText: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Did you know? \n\n$factText\n\nShared from BrainBites 🧠")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share this Psychology Fact")
        context.startActivity(shareIntent)
        
        CoroutineScope(Dispatchers.IO).launch {
            BiteRepository.incrementShares(context)
        }
    }

    fun shareFactAsImage(context: Context, bitmap: Bitmap, factText: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting image generation for sharing...")
                val imagesFolder = File(context.cacheDir, "shared_images")
                if (!imagesFolder.exists()) {
                    val created = imagesFolder.mkdirs()
                    Log.d(TAG, "Created shared_images folder: $created")
                }
                
                val file = File(imagesFolder, "shared_fact_${System.currentTimeMillis()}.png")
                val stream = FileOutputStream(file)
                val compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.flush()
                stream.close()
                Log.d(TAG, "Bitmap saved to file: ${file.absolutePath} (Success: $compressed)")

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                Log.d(TAG, "Generated FileProvider URI: $uri")
                
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "Shared from BrainBites 🧠\n\n$factText")
                    type = "image/png"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                withContext(Dispatchers.Main) {
                    val shareIntent = Intent.createChooser(sendIntent, "Share Insight Card")
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(shareIntent)
                    Log.d(TAG, "Share Intent launched on Main thread")
                }
                
                BiteRepository.incrementShares(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error in Smart Sharing", e)
                e.printStackTrace()
            }
        }
    }

    fun shareApp(context: Context) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Download BrainBites to learn fascinating psychology facts every day! 🧠 \n\nhttps://play.google.com/store/apps/details?id=com.example.brainbites")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share BrainBites")
        context.startActivity(shareIntent)

        CoroutineScope(Dispatchers.IO).launch {
            BiteRepository.incrementShares(context)
        }
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
