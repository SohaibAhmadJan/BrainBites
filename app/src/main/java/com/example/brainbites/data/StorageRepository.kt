package com.example.brainbites.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object StorageRepository {
    // Stable Default Instance: Uses bucket from google-services.json
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadProfilePicture(context: Context, uid: String, localUri: Uri): Result<String> {
        return try {
            Log.d("StorageRepository", "🚀 STEP 1: Reading bytes for $localUri")
            val bytes = context.contentResolver.openInputStream(localUri)?.use { it.readBytes() }
                ?: throw Exception("Could not read image data.")

            // Path matches flattened security rules
            val storageRef = storage.reference.child("profile_pics/$uid.jpg")
            
            Log.d("StorageRepository", "🚀 STEP 2: Uploading to bucket: ${storage.reference.bucket}")
            storageRef.putBytes(bytes).await()
            
            Log.d("StorageRepository", "🚀 STEP 3: Finalizing Public URL")
            val downloadUrl = storageRef.downloadUrl.await().toString()
            
            Log.d("StorageRepository", "✅ SUCCESS: $downloadUrl")
            Result.success(downloadUrl)
        } catch (e: com.google.firebase.storage.StorageException) {
            val detailedError = "Firebase Storage Error [${e.errorCode}]: ${e.message}"
            Log.e("StorageRepository", detailedError, e)
            Result.failure(Exception(detailedError))
        } catch (e: Exception) {
            Log.e("StorageRepository", "❌ FAILED: ${e.message}", e)
            Result.failure(e)
        }
    }
}
