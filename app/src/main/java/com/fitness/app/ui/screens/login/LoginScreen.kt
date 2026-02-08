package com.fitness.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent
import android.net.Uri
import com.fitness.app.auth.GoogleAuthCodeStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val googleCode by GoogleAuthCodeStore.code.collectAsState()

    // Matching mockup colors
    val bgColor = Color(0xFFF0F4F8)
    val cardBg = Color.White
    val accentDark = Color(0xFF343E4E)
    val labelColor = Color(0xFF343E4E)
    val inputBg = Color(0xFFF8FAFC)
    val inputBorder = Color(0xFFE2E8F0)

    LaunchedEffect(googleCode) {
        val code = googleCode
        if (!code.isNullOrBlank()) {
            viewModel.onGoogleCodeReceived(code, onLoginSuccess)
            GoogleAuthCodeStore.clear()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            // Logo Icon
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = accentDark
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp)
            )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App Name
            Text(
                text = "FitTrack",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = accentDark,
                    fontSize = 30.sp
                )
            )

            Text(
                text = "Your personal fitness companion",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = accentDark,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome back",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentDark,
                            fontSize = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Enter your credentials to access your account",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.onEmailChanged(it) },
                            placeholder = { Text("Email", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = uiState.emailError != null,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = inputBg,
                                unfocusedContainerColor = inputBg,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = inputBorder,
                                unfocusedBorderColor = inputBorder
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                        uiState.emailError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.onPasswordChanged(it) },
                            placeholder = { Text("Password", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                }
                            },
                            isError = uiState.passwordError != null,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = inputBg,
                                unfocusedContainerColor = inputBg,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = inputBorder,
                                unfocusedBorderColor = inputBorder
                            ),
                            shape = RoundedCornerShape(4.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                        uiState.passwordError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sign In Button
                    Button(
                        onClick = { viewModel.onSignInClicked(onLoginSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentDark)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "or",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Sign In Button
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://node86.cs.colman.ac.il/api/auth/google")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = accentDark)
                    ) {
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = accentDark,
                                    fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Don't have an account? Sign up",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = accentDark,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.clickable { onNavigateToSignup() }
                    )
                }
            }
        }
    }

    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            },
            title = { Text("Sign In Failed") },
            text = { Text(uiState.errorMessage ?: "Login failed") }
        )
    }
}
