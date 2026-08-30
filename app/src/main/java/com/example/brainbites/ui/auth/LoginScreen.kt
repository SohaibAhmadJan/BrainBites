package com.example.brainbites.ui.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.brainbites.R
import com.example.brainbites.data.AuthRepository
import com.example.brainbites.ui.theme.BrainBitesTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var isGuestLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val credentialManager = CredentialManager.create(context)

    PremiumAuthBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // App Icon Box
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Sign in to access your saved insights and\ndaily growth.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                AuthCard {
                    PremiumTextField(
                        value = email,
                        onValueChange = { email = it; error = null },
                        label = "Email address",
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        modifier = Modifier.padding(top = 0.dp)
                    )

                    PremiumTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = "Password",
                        icon = Icons.Default.Lock,
                        isPassword = true,
                        imeAction = ImeAction.Done
                    )

                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { 
                                if (email.isBlank()) {
                                    Toast.makeText(context, "Enter your email first", Toast.LENGTH_SHORT).show()
                                } else {
                                    scope.launch {
                                        val result = AuthRepository.sendPasswordResetEmail(email)
                                        if (result.isSuccess) {
                                            Toast.makeText(context, "Reset link sent to $email", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(0.dp))

                    MainActionButton(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                error = "Required fields missing"
                                return@MainActionButton
                            }
                            isLoading = true
                            scope.launch {
                                val result = AuthRepository.signIn(context, email, password)
                                isLoading = false
                                if (result.isSuccess) onLoginSuccess()
                                else error = result.exceptionOrNull()?.message
                            }
                        },
                        text = "Sign In",
                        isLoading = isLoading
                    )

                    AuthDivider()

                    GoogleButton(
                        onClick = {
                            val serverClientId = try {
                                context.getString(R.string.default_web_client_id)
                            } catch (e: Exception) {
                                ""
                            }
                            
                            if (serverClientId.isEmpty() || serverClientId.contains("xxxx")) {
                                error = "Google Sign-In is not configured. Please add your Web Client ID to strings.xml."
                                Log.e("LoginScreen", "Invalid Web Client ID: $serverClientId")
                                return@GoogleButton
                            }

                            isGoogleLoading = true
                            scope.launch {
                                try {
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(serverClientId)
                                        .setAutoSelectEnabled(true)
                                        .build()

                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()

                                    val result = credentialManager.getCredential(context, request)
                                    val googleIdToken = (result.credential as? androidx.credentials.CustomCredential)?.data?.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")
                                    
                                    if (googleIdToken != null) {
                                        val authResult = AuthRepository.signInWithGoogle(context, googleIdToken)
                                        if (authResult.isSuccess) onLoginSuccess()
                                        else error = authResult.exceptionOrNull()?.message
                                    }
                                } catch (e: Exception) {
                                    error = e.message
                                } finally {
                                    isGoogleLoading = false
                                }
                            }
                        },
                        isLoading = isGoogleLoading
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " Sign Up",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToSignUp() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isGuestLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Continue as Guest",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.clickable {
                            isGuestLoading = true
                            scope.launch {
                                val result = AuthRepository.signInAnonymously(context)
                                isGuestLoading = false
                                if (result.isSuccess) onLoginSuccess()
                                else error = result.exceptionOrNull()?.message
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Error Feedback
            if (error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
                ) {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    BrainBitesTheme {
        LoginScreen(onLoginSuccess = {}, onNavigateToSignUp = {})
    }
}
