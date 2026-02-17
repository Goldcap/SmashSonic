package com.smashsonic.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smashsonic.ui.components.SmashSonicBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSetupScreen(
    isInitialSetup: Boolean = false,
    onConfigured: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    viewModel: ServerSetupViewModel = hiltViewModel(),
) {
    val serverURL by viewModel.serverURL.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val isTesting by viewModel.isTesting.collectAsState()
    val testResult by viewModel.testResult.collectAsState()

    SmashSonicBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isInitialSetup) "Connect to Server" else "Settings") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    ),
                )
            },
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (!isInitialSetup) {
                    // Appearance navigation
                    Card(
                        onClick = onNavigateToAppearance,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null)
                            Text("Appearance", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }

                // Server Connection section
                Text("Server Connection", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = serverURL,
                    onValueChange = viewModel::updateServerURL,
                    label = { Text("Server URL") },
                    placeholder = { Text("https://music.example.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = viewModel::updateUsername,
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = viewModel::updatePassword,
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Test Connection button
                OutlinedButton(
                    onClick = { viewModel.testConnection() },
                    enabled = !isTesting && viewModel.isFormValid,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Test Connection")
                    Spacer(Modifier.width(8.dp))
                    when {
                        isTesting -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        testResult is ServerSetupViewModel.TestResult.Success -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        testResult is ServerSetupViewModel.TestResult.Failure -> Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                }

                if (testResult is ServerSetupViewModel.TestResult.Failure) {
                    Text(
                        (testResult as ServerSetupViewModel.TestResult.Failure).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                // Save button
                Button(
                    onClick = {
                        viewModel.saveConfiguration()
                        onConfigured()
                    },
                    enabled = viewModel.isFormValid,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save")
                }

                // Sign Out button
                if (!isInitialSetup) {
                    Spacer(Modifier.height(16.dp))
                    TextButton(
                        onClick = {
                            viewModel.signOut()
                            onSignOut()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Text("Sign Out")
                    }
                }
            }
        }
    }
}
