package com.example.brainbites.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

object AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _currentUser = MutableStateFlow<BrainBitesUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isAccountDisabled = MutableStateFlow(false)
    val isAccountDisabled = _isAccountDisabled.asStateFlow()

    suspend fun signInAnonymously() {
        if (auth.currentUser == null) {
            try {
                auth.signInAnonymously().await()
                Log.d("AuthRepository", "Signed in anonymously: ${auth.currentUser?.uid}")
            } catch (e: Exception) {
                Log.e("AuthRepository", "Anonymous sign in failed", e)
            }
        }
        syncUser()
    }

    fun syncUser() {
        val firebaseUser = auth.currentUser ?: return
        val uid = firebaseUser.uid

        // Start real-time listener for user document (Account status \u0026 Profile)
        db.collection("users").document(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("AuthRepository", "User sync listen failed", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val profile = snapshot.get("profile") as? Map<*, *>
                        val stats = snapshot.get("stats") as? Map<*, *>
                        val prefs = snapshot.get("preferences") as? Map<*, *>
                        val account = snapshot.get("account") as? Map<*, *>

                        val brainBitesUser = BrainBitesUser(
                            account = UserAccount(
                                uid = uid,
                                createdAt = account?.get("createdAt") as? Long ?: System.currentTimeMillis(),
                                updatedAt = account?.get("updatedAt") as? Long ?: System.currentTimeMillis(),
                                lastLoginAt = account?.get("lastLoginAt") as? Long ?: System.currentTimeMillis(),
                                status = account?.get("status") as? String ?: "ACTIVE"
                            ),
                            profile = UserProfile(
                                displayName = profile?.get("displayName") as? String ?: "Knowledge Seeker",
                                email = firebaseUser.email ?: "",
                                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                                bio = profile?.get("bio") as? String ?: "",
                                isPublic = profile?.get("isPublic") as? Boolean ?: false
                            ),
                            stats = UserStats(
                                streakCount = (stats?.get("streakCount") as? Long)?.toInt() ?: 0,
                                factsReadCount = (stats?.get("factsReadCount") as? Long)?.toInt() ?: 0,
                                favoritesCount = (stats?.get("favoritesCount") as? Long)?.toInt() ?: 0,
                                sharesCount = (stats?.get("sharesCount") as? Long)?.toInt() ?: 0,
                                lastActiveAt = stats?.get("lastActiveAt") as? Long ?: 0
                            ),
                            preferences = UserPreferences(
                                dailyGoal = (prefs?.get("dailyGoal") as? Long)?.toInt() ?: 5,
                                textScale = (prefs?.get("textScale") as? Double)?.toFloat() ?: 1.0f,
                                hapticsEnabled = prefs?.get("hapticsEnabled") as? Boolean ?: true,
                                analyticsEnabled = prefs?.get("analyticsEnabled") as? Boolean ?: true,
                                notificationsEnabled = prefs?.get("notificationsEnabled") as? Boolean ?: true
                            )
                        )
                        _currentUser.value = brainBitesUser
                        _isAccountDisabled.value = brainBitesUser.account.status == "DISABLED"
                    } catch (ex: Exception) {
                        Log.e("AuthRepository", "Error mapping user data", ex)
                    }
                } else {
                    // Create new user record if it doesn\u0027t exist
                    val now = System.currentTimeMillis()
                    val newUser = BrainBitesUser(
                        account = UserAccount(
                            uid = uid,
                            createdAt = now,
                            updatedAt = now,
                            lastLoginAt = now,
                            status = "ACTIVE"
                        )
                    )
                    MainScope().launch {
                        saveUser(newUser)
                    }
                }
            }
    }

    suspend fun updateLastActive() {
        val uid = auth.currentUser?.uid ?: return
        try {
            db.collection("users").document(uid)
                .update("stats.lastActiveAt", System.currentTimeMillis())
                .await()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error updating last active", e)
        }
    }

    suspend fun saveUser(user: BrainBitesUser) {
        try {
            db.collection("users").document(user.account.uid).set(
                mapOf(
                    "account" to user.account,
                    "profile" to user.profile,
                    "stats" to user.stats,
                    "preferences" to user.preferences,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            _currentUser.value = user
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error saving user", e)
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    suspend fun syncDeviceToken(context: Context, token: String) {
        val uid = auth.currentUser?.uid ?: return
        val prefs = context.getSharedPreferences("brain_bites_device", Context.MODE_PRIVATE)
        var deviceId = prefs.getString("device_id", "")
        if (deviceId.isNullOrEmpty()) {
            deviceId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString("device_id", deviceId).apply()
        }

        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val appVersion = packageInfo.versionName

        val deviceData = mapOf(
            "fcmToken" to token,
            "platform" to "android",
            "appVersion" to appVersion,
            "updatedAt" to System.currentTimeMillis(),
            "lastSeenAt" to System.currentTimeMillis()
        )

        try {
            db.collection("users").document(uid).collection("devices").document(deviceId!!)
                .set(deviceData, com.google.firebase.firestore.SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error syncing device token", e)
        }
    }
}
