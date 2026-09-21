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

    suspend fun verifySession() {
        val firebaseUser = auth.currentUser ?: return
        try {
            firebaseUser.reload().await()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to reload user session (likely deleted or disabled). Signing out locally.", e)
            signOut()
        }
    }

    suspend fun isUsernameAvailable(handle: String): Boolean {
        if (handle.isBlank()) return false
        val normalizedHandle = handle.lowercase().trim()
        return try {
            val document = db.collection("handles").document(normalizedHandle).get().await()
            !document.exists()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error checking handle availability", e)
            false // Default to not available on error to be safe
        }
    }

    suspend fun signInAnonymously(context: Context): Result<Unit> {
        return try {
            if (auth.currentUser == null) {
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    Log.d("AuthRepository", "Offline detected via NetworkUtils. Creating local guest session.")
                    createLocalGuestSession(context)
                    return Result.success(Unit)
                }

                try {
                    auth.signInAnonymously().await()
                    Log.d("AuthRepository", "Signed in anonymously: ${auth.currentUser?.uid}")
                } catch (e: Exception) {
                    Log.w("AuthRepository", "Firebase Auth network call failed. Falling back to local guest session.", e)
                    createLocalGuestSession(context)
                    return Result.success(Unit)
                }
            }
            syncUser(context)
            updateLastActive()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Anonymous sign in failed, falling back to local guest session", e)
            createLocalGuestSession(context)
            Result.success(Unit)
        }
    }

    private fun createLocalGuestSession(context: Context) {
        val localUid = "local_guest_" + System.currentTimeMillis()
        val now = System.currentTimeMillis()
        val localUser = BrainBitesUser(
            account = UserAccount(
                uid = localUid,
                createdAt = now,
                updatedAt = now,
                lastLoginAt = now,
                status = "ACTIVE"
            ),
            profile = UserProfile(
                displayName = "Local Guest",
                email = "",
                handle = "guest_${now.toString().takeLast(6)}"
            )
        )
        val prefs = context.getSharedPreferences("brain_bites_device", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pending_online_provisioning", true).apply()

        _currentUser.value = localUser
        PreferenceManager.syncWithServer(localUser)
    }

    fun startNetworkProvisioningObserver(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager ?: return
        val request = android.net.NetworkRequest.Builder()
            .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    super.onAvailable(network)
                    val prefs = context.getSharedPreferences("brain_bites_device", Context.MODE_PRIVATE)
                    val isPending = prefs.getBoolean("pending_online_provisioning", false)
                    if (isPending || auth.currentUser == null) {
                        MainScope().launch {
                            try {
                                if (auth.currentUser == null) {
                                    auth.signInAnonymously().await()
                                    Log.d("AuthRepository", "Background Provisioning: Signed in anonymously upon reconnection.")
                                }
                                syncUser(context)
                                updateLastActive()
                                pushUserDataToServer(context)
                                prefs.edit().putBoolean("pending_online_provisioning", false).apply()
                                Log.d("AuthRepository", "Background Provisioning: Successfully synced local guest to cloud.")
                            } catch (e: Exception) {
                                Log.e("AuthRepository", "Background Provisioning failed upon reconnection", e)
                            }
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error registering network callback", e)
        }
    }

    suspend fun signInWithGoogle(context: Context, idToken: String, isSignUpFlow: Boolean = false): Result<Unit> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: throw Exception("Google Sign-In failed")
            val isNewUser = result.additionalUserInfo?.isNewUser == true
            val uid = firebaseUser.uid

            if (!isSignUpFlow && isNewUser) {
                // Log In flow but user is new
                try {
                    firebaseUser.delete().await()
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Failed to delete auto-created user", e)
                }
                auth.signOut()
                return Result.failure(Exception("Account does not exist. Please use Sign Up."))
            }

            if (isSignUpFlow && !isNewUser) {
                // Sign Up flow but user already exists
                auth.signOut()
                return Result.failure(Exception("Account already exists. Please log in."))
            }

            // Create user document if it does not exist (or if it's missing profile)
            val userDoc = db.collection("users").document(uid).get().await()
            if (!userDoc.exists() || !userDoc.contains("profile")) {
                val now = System.currentTimeMillis()
                val randomHandle = "user_${now.toString().takeLast(6)}"
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
                        email = firebaseUser.email ?: "",
                        handle = randomHandle,
                        photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                    )
                )

                try {
                    db.collection("users").document(uid).set(
                        mapOf(
                            "account" to newUser.account,
                            "profile" to newUser.profile,
                            "stats" to newUser.stats,
                            "preferences" to newUser.preferences,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    ).await()
                    
                    try {
                        db.collection("handles").document(randomHandle).set(mapOf("uid" to uid)).await()
                    } catch (e: Exception) {
                        Log.e("AuthRepository", "Failed to claim handle during Google Sign-up", e)
                    }
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Error creating Google user document", e)
                }
            }

            syncUser(context)
            updateLastActive()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(context: Context, email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            syncUser(context)
            updateLastActive()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is com.google.firebase.auth.FirebaseAuthInvalidUserException || 
                (e is com.google.firebase.auth.FirebaseAuthException && e.errorCode == "ERROR_USER_NOT_FOUND")) {
                Result.failure(Exception("Account does not exist. Please use Sign Up."))
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun signUp(context: android.content.Context, email: String, password: String, name: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("User creation failed")
            val uid = firebaseUser.uid
            
            // Update Firebase Auth profile immediately to prevent syncUser race conditions
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
            
            val now = System.currentTimeMillis()
            val randomHandle = "user_${now.toString().takeLast(6)}"
            val newUser = BrainBitesUser(
                account = UserAccount(
                    uid = uid,
                    createdAt = now,
                    updatedAt = now,
                    lastLoginAt = now,
                    status = "ACTIVE"
                ),
                profile = UserProfile(
                    displayName = name.ifBlank { "Knowledge Seeker" },
                    email = email,
                    handle = randomHandle,
                    photoUrl = "" // Explicitly set to empty so the UI falls back to placeholder
                )
            )
            
            MainScope().launch {
                var success = false
                var retries = 0
                while (!success && retries < 3) {
                    try {
                        db.runBatch { batch ->
                            val handleRef = db.collection("handles").document(randomHandle)
                            val userRef = db.collection("users").document(uid)
                            
                            batch.set(handleRef, mapOf("uid" to uid))
                            batch.set(userRef, mapOf(
                                "account" to newUser.account,
                                "profile" to newUser.profile,
                                "stats" to newUser.stats,
                                "preferences" to newUser.preferences,
                                "updatedAt" to System.currentTimeMillis()
                            ))
                        }.await()
                        AnalyticsRepository.logAppInstall(context)
                        success = true
                    } catch (e: Exception) {
                        retries++
                        Log.e("AuthRepository", "Failed to claim handle or save user (Auth propagation delay?), retrying... ($retries/3)", e)
                        kotlinx.coroutines.delay(1000)
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is com.google.firebase.auth.FirebaseAuthUserCollisionException || 
                (e is com.google.firebase.auth.FirebaseAuthException && e.errorCode == "ERROR_EMAIL_ALREADY_IN_USE")) {
                Result.failure(Exception("Account already exists. Please log in."))
            } else {
                Result.failure(e)
            }
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
                                handle = profile?.get("handle") as? String ?: "",
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
                    val randomHandle = "user_${now.toString().takeLast(6)}"
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
                            email = firebaseUser.email ?: "",
                            handle = randomHandle,
                            photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                        )
                    )
                    MainScope().launch {
                        var success = false
                        var retries = 0
                        while (!success && retries < 3) {
                            try {
                                // Claim random handle
                                db.collection("handles").document(randomHandle).set(mapOf("uid" to uid)).await()
                                saveUser(newUser)
                                AnalyticsRepository.logAppInstall(context)
                                val instanceId = com.google.firebase.installations.FirebaseInstallations.getInstance().id.await()
                                syncDeviceToken(context, instanceId)
                                success = true
                            } catch (e: Exception) {
                                retries++
                                Log.e("AuthRepository", "Failed to claim handle or save user (Auth propagation delay?), retrying... ($retries/3)", e)
                                kotlinx.coroutines.delay(1000)
                            }
                        }
                    }
                }
            }
    }

    suspend fun updateLastActive() {
        val uid = auth.currentUser?.uid ?: return
        try {
            db.collection("users").document(uid)
                .update(
                    mapOf(
                        "stats.lastActiveAt" to System.currentTimeMillis(),
                        "account.lastLoginAt" to System.currentTimeMillis(),
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Log.d("AuthRepository", "Global activity ping SUCCESS for $uid")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error updating last active, attempting merge...", e)
            // Fallback: If document or stats map doesn't exist, create/merge it
            db.collection("users").document(uid)
                .set(mapOf("stats" to mapOf("lastActiveAt" to System.currentTimeMillis())), com.google.firebase.firestore.SetOptions.merge())
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

    suspend fun deleteAccount(): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No user logged in"))
        val currentHandle = _currentUser.value?.profile?.handle ?: ""
        
        return try {
            val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
            val deletionDate = System.currentTimeMillis() + thirtyDaysMs

            // Atomic Anonymization + Handle Release
            db.runBatch { batch ->
                val userRef = db.collection("users").document(uid)
                
                // 1. Mark as pending and wipe PII immediately
                batch.update(userRef, mapOf(
                    "account.status" to "PENDING_DELETION",
                    "account.scheduledDeletionAt" to deletionDate,
                    "profile.displayName" to "Deleted User",
                    "profile.email" to "",
                    "profile.bio" to "",
                    "profile.photoUrl" to "",
                    "profile.handle" to "",
                    "updatedAt" to System.currentTimeMillis()
                ))

                // 2. Release the @handle so others can use it
                if (currentHandle.isNotBlank()) {
                    val handleRef = db.collection("handles").document(currentHandle)
                    batch.delete(handleRef)
                }
            }.await()
            
            // 3. Destroy Firebase Auth Credential immediately
            auth.currentUser?.delete()?.await()
            
            _currentUser.value = null
            Log.d("AuthRepository", "Account anonymized and scheduled for deletion")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Account deletion/anonymization failed", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(name: String, bio: String, image: String, newHandle: String? = null): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No user logged in"))
        val currentHandle = _currentUser.value?.profile?.handle ?: ""
        
        return try {
            val normalizedNewHandle = newHandle?.lowercase()?.trim()
            
            // Use batch write to ensure we don't end up with orphaned handles or users without handles
            db.runBatch { batch ->
                val userRef = db.collection("users").document(uid)
                
                val updates = mutableMapOf<String, Any>(
                    "profile.displayName" to name,
                    "profile.bio" to bio,
                    "profile.photoUrl" to image,
                    "updatedAt" to System.currentTimeMillis()
                )

                // Only perform handle swap if it's changing and valid
                if (normalizedNewHandle != null && normalizedNewHandle != currentHandle) {
                    val newHandleRef = db.collection("handles").document(normalizedNewHandle)
                    batch.set(newHandleRef, mapOf("uid" to uid)) // Claim new
                    
                    if (currentHandle.isNotBlank()) {
                        val oldHandleRef = db.collection("handles").document(currentHandle)
                        batch.delete(oldHandleRef) // Release old
                    }
                    
                    updates["profile.handle"] = normalizedNewHandle
                }

                batch.update(userRef, updates)
            }.await()

            Log.d("AuthRepository", "Profile updated in Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            // Note: If the new handle is already claimed, the batch will fail gracefully
            // due to Firestore security rules, assuming we don't have read access to it
            // or the create rule fails.
            Log.e("AuthRepository", "Error updating profile in Firestore", e)
            Result.failure(Exception(e.message?.let { if (it.contains("PERMISSION_DENIED")) "Handle is already taken or invalid." else it } ?: "Unknown error"))
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
