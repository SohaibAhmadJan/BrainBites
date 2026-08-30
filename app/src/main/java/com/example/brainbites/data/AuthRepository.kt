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

    suspend fun signInAnonymously(context: Context): Result<Unit> {
        return try {
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
                Log.d("AuthRepository", "Signed in anonymously: ${auth.currentUser?.uid}")
            }
            syncUser(context)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Anonymous sign in failed", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(context: Context, idToken: String): Result<Unit> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            syncUser(context)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(context: Context, email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            syncUser(context)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(context: android.content.Context, email: String, password: String, name: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User creation failed")
            
            val now = System.currentTimeMillis()
            val newUser = BrainBitesUser(
                account = UserAccount(
                    uid = uid,
                    createdAt = now,
                    updatedAt = now,
                    lastLoginAt = now,
                    status = "ACTIVE"
                ),
                profile = UserProfile(
                    displayName = name,
                    email = email
                )
            )
            saveUser(newUser)
            AnalyticsRepository.logAppInstall(context)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun syncUser(context: Context) {
        val firebaseUser = auth.currentUser ?: return
        val uid = firebaseUser.uid

        // Start real-time listener for user document (Account status & Profile)
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
                                displayName = profile?.get("displayName") as? String ?: firebaseUser.displayName ?: "Knowledge Seeker",
                                email = profile?.get("email") as? String ?: firebaseUser.email ?: "",
                                photoUrl = profile?.get("photoUrl") as? String ?: firebaseUser.photoUrl?.toString() ?: "",
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

                        // Smart Sync: Backfill missing email if it exists in Firebase Auth
                        val existingEmail = profile?.get("email") as? String
                        if (existingEmail.isNullOrEmpty() && !firebaseUser.email.isNullOrEmpty()) {
                            MainScope().launch {
                                db.collection("users").document(uid)
                                    .update("profile.email", firebaseUser.email)
                                    .await()
                                Log.d("AuthRepository", "Smart Sync: Backfilled email for $uid")
                            }
                        }

                        _currentUser.value = brainBitesUser
                        _isAccountDisabled.value = brainBitesUser.account.status == "DISABLED"

                        // Device Hardening: Ensure current device record is active and up to date
                        MainScope().launch {
                            val instanceId = com.google.firebase.installations.FirebaseInstallations.getInstance().id.await()
                            syncDeviceToken(context, instanceId)
                        }
                    } catch (ex: Exception) {
                        Log.e("AuthRepository", "Error mapping user data", ex)
                    }
                } else {
                    // Create new user record if it doesn't exist
                    val now = System.currentTimeMillis()
                    val newUser = BrainBitesUser(
                        account = UserAccount(
                            uid = uid,
                            createdAt = now,
                            updatedAt = now,
                            lastLoginAt = now,
                            status = "ACTIVE"
                        ),
                        profile = UserProfile(
                            displayName = firebaseUser.displayName ?: "Knowledge Seeker",
                            email = firebaseUser.email ?: ""
                        )
                    )
                    MainScope().launch {
                        saveUser(newUser)
                        AnalyticsRepository.logAppInstall(context)
                        val instanceId = com.google.firebase.installations.FirebaseInstallations.getInstance().id.await()
                        syncDeviceToken(context, instanceId)
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

    suspend fun updateUserProfile(name: String, bio: String, image: String) {
        val uid = auth.currentUser?.uid ?: return
        try {
            db.collection("users").document(uid).update(
                mapOf(
                    "profile.displayName" to name,
                    "profile.bio" to bio,
                    "profile.photoUrl" to image,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Log.d("AuthRepository", "Profile updated in Firestore")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error updating profile in Firestore", e)
        }
    }

    suspend fun updateUserPreferences(dailyGoal: Int? = null, textScale: Float? = null, haptics: Boolean? = null, analytics: Boolean? = null, notifications: Boolean? = null) {
        val uid = auth.currentUser?.uid ?: return
        val updates = mutableMapOf<String, Any>()
        dailyGoal?.let { updates["preferences.dailyGoal"] = it }
        textScale?.let { updates["preferences.textScale"] = it }
        haptics?.let { updates["preferences.hapticsEnabled"] = it }
        analytics?.let { updates["preferences.analyticsEnabled"] = it }
        notifications?.let { updates["preferences.notificationsEnabled"] = it }
        
        if (updates.isEmpty()) return
        
        updates["updatedAt"] = System.currentTimeMillis()
        
        try {
            db.collection("users").document(uid).update(updates).await()
            Log.d("AuthRepository", "Preferences updated in Firestore")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error updating preferences in Firestore", e)
        }
    }

    suspend fun pushUserDataToServer(context: Context) {
        val uid = auth.currentUser?.uid ?: return
        val current = _currentUser.value ?: return
        
        try {
            db.collection("users").document(uid).set(
                mapOf(
                    "account" to current.account.copy(updatedAt = System.currentTimeMillis()),
                    "profile" to current.profile,
                    "stats" to current.stats,
                    "preferences" to current.preferences,
                    "updatedAt" to System.currentTimeMillis()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            
            // Ensure device is synced during data push
            val token = com.google.firebase.installations.FirebaseInstallations.getInstance().id.await()
            syncDeviceToken(context, token)
            
            Log.d("AuthRepository", "User data and device pushed to server successfully")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error pushing user data to server", e)
        }
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
