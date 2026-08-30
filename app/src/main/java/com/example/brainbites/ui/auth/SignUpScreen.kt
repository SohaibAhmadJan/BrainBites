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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
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
                Spacer(modifier = Modifier.height(32.dp))

                // App Icon Box
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Create Your Account",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Join thousands of others on a journey\nto understand the human mind.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                AuthCard {
                    PremiumTextField(
                        value = name,
                        onValueChange = { name = it; error = null },
                        label = "Full Name",
                        icon = Icons.Default.Person
                    )

                    PremiumTextField(
                        value = email,
                        onValueChange = { email = it; error = null },
                        label = "Email address",
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email
                    )

                    Column {
                        PremiumTextField(
                            value = password,
                            onValueChange = { password = it; error = null },
                            label = "Password",
                            icon = Icons.Default.Lock,
                            isPassword = true,
                            imeAction = ImeAction.Done
                        )
                        PasswordStrengthBar(password)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.outline,
                                checkmarkColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        
                        val annotatedString = buildAnnotatedString {
                            append("By signing up, you agree to our ")
                            pushStringAnnotation(tag = "terms", annotation = "terms")
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                                append("Terms")
                            }
                            pop()
                            append(" and ")
                            pushStringAnnotation(tag = "privacy", annotation = "privacy")
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                                append("Privacy Policy")
                            }
                            pop()
                            append(".")
                        }

                        ClickableText(
                            text = annotatedString,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)),
                            onClick = { offset ->
                                annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset).firstOrNull()?.let {
                                    Toast.makeText(context, "Terms of Service", Toast.LENGTH_SHORT).show()
                                }
                                annotatedString.getStringAnnotations(tag = "privacy", start = offset, end = offset).firstOrNull()?.let {
                                    Toast.makeText(context, "Privacy Policy", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    MainActionButton(
                        onClick = {
                            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                                error = "All fields required"
                                return@MainActionButton
                            }
                            if (!termsAccepted) {
                                error = "Please accept Terms \u0026 Privacy"
                                return@MainActionButton
                            }
                            isLoading = true
                            scope.launch {
                                val result = AuthRepository.signUp(context, email, password, name)
                                isLoading = false
                                if (result.isSuccess) onSignUpSuccess()
                                else error = result.exceptionOrNull()?.message
                            }
                        },
                        text = "Create Account",
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
                                Log.e("SignUpScreen", "Invalid Web Client ID: $serverClientId")
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
                                        if (authResult.isSuccess) onSignUpSuccess()
                                        else error = authResult.exceptionOrNull()?.message
                                    }
                                } catch (e: Exception) {
                                    error = e.message
                                } finally {
                                    isGoogleLoading = false
                                }
                            }
                        }, 
                        text = "Sign up with Google",
                        isLoading = isGoogleLoading
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " Log In",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Error Feedback Overlay
            if (error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp)
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
fun SignUpScreenPreview() {
    BrainBitesTheme {
        SignUpScreen(onSignUpSuccess = {}, onNavigateToLogin = {})
    }
}
