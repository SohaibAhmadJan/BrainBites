package com.example.brainbites.data

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object StorageRepository {
    // Cloudinary Config (From Admin Panel .env)
    private const val CLOUDINARY_CLOUD_NAME = "o884wjpk"
    private const val CLOUDINARY_UPLOAD_PRESET = "ml_default"
    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload"

    suspend fun uploadProfilePicture(context: Context, uid: String, localUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("StorageRepository", "🚀 STEP 1: Uploading file directly from URI to Cloudinary: $localUri")

                val inputStream = context.contentResolver.openInputStream(localUri)
                    ?: throw Exception("Could not open input stream for URI")

                val boundary = "----CloudinaryFormBoundary${UUID.randomUUID()}"
                val connection = URL(UPLOAD_URL).openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

                DataOutputStream(connection.outputStream).use { outputStream ->
                    // 1. Add upload_preset
                    outputStream.writeBytes("--$boundary\r\n")
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
                    outputStream.writeBytes("$CLOUDINARY_UPLOAD_PRESET\r\n")

                    // 2. Add public_id (Optional: to keep URLs clean and overwrite old ones)
                    outputStream.writeBytes("--$boundary\r\n")
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"public_id\"\r\n\r\n")
                    outputStream.writeBytes("profile_pics/$uid\r\n")

                    // 3. Add file data
                    outputStream.writeBytes("--$boundary\r\n")
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"$uid.jpg\"\r\n")
                    outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n")

                    // Read from URI and write to network stream
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.writeBytes("\r\n")
                    
                    // End boundary
                    outputStream.writeBytes("--$boundary--\r\n")
                    outputStream.flush()
                }
                inputStream.close()

                Log.d("StorageRepository", "🚀 STEP 2: Waiting for Cloudinary response...")
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseStr = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseStr)
                    val secureUrl = json.getString("secure_url")
                    
                    Log.d("StorageRepository", "✅ SUCCESS: $secureUrl")
                    Result.success(secureUrl)
                } else {
                    val errorStream = connection.errorStream ?: connection.inputStream
                    val errorResponse = errorStream.bufferedReader().use { it.readText() }
                    Log.e("StorageRepository", "❌ FAILED: HTTP $responseCode - $errorResponse")
                    Result.failure(Exception("Cloudinary upload failed: HTTP $responseCode"))
                }
            } catch (e: Exception) {
                Log.e("StorageRepository", "❌ FAILED: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}
