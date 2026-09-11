package com.example.brainbites.ui.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainbites.ui.components.AnimatedEntrance
import com.example.brainbites.ui.components.LottieBackground
import com.example.brainbites.ui.theme.DeepForest
import com.example.brainbites.ui.theme.PureWhite
import com.example.brainbites.ui.theme.DarkSurface
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun DeleteAccountScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isConfirmed by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var isSimulationMode by remember { mutableStateOf(true) } // Simulation mode ON by default for testing
    var simulationMessage by remember { mutableStateOf<String?>(null) }
    
    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxSize()) {
        LottieBackground()
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background.copy(alpha = 0.65f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedEntrance(index = 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    border = BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = colorScheme.errorContainer.copy(alpha = 0.5f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = colorScheme.error,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = if (simulationMessage != null) "Deletion Scheduled" else "Delete Account?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (simulationMessage != null) 
                                simulationMessage!! 
                            else 
                                "Your account will be marked for deletion. You will have 30 days to cancel this request before all your data is permanently removed.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )

                        if (simulationMessage == null) {
                            Spacer(modifier = Modifier.height(32.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isConfirmed,
                                        onCheckedChange = { isConfirmed = it },
                                        colors = CheckboxDefaults.colors(checkedColor = colorScheme.error)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "I understand that my data will be scheduled for permanent deletion.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = {
                                    if (isConfirmed) {
                                        if (isSimulationMode) {
                                            simulationMessage = "SIMULATION: Account scheduled for deletion. Access will be revoked in 30 days. Permanent purge scheduled for ${Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)}"
                                        } else {
                                            isDeleting = true
                                            scope.launch {
                                                val result = viewModel.deleteAccount()
                                                isDeleting = false
                                                if (result.isSuccess) {
                                                    Toast.makeText(context, "Deletion request submitted", Toast.LENGTH_LONG).show()
                                                    onAccountDeleted()
                                                } else {
                                                    Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    }
                                },
                                enabled = isConfirmed && !isDeleting,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.error,
                                    contentColor = colorScheme.onError
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                if (isDeleting) {
                                    CircularProgressIndicator(color = colorScheme.onError, modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(if (isSimulationMode) "Simulate Deletion" else "Schedule Deletion", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (simulationMessage != null) "Close" else "Cancel", color = colorScheme.primary)
                        }
                        
                        // Simulation Toggle
                        if (simulationMessage == null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Simulation Mode", style = MaterialTheme.typography.labelSmall)
                                Switch(
                                    checked = isSimulationMode, 
                                    onCheckedChange = { isSimulationMode = it },
                                    modifier = Modifier.scale(0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
