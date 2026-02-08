package com.fitness.app.ui.screens.signup

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitness.app.ui.components.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: SignupViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val bgColor = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val accentDark = MaterialTheme.colorScheme.onBackground
    val labelColor = MaterialTheme.colorScheme.onBackground
    val inputBg = Color(0xFFF8FAFC)
    val inputBorder = MaterialTheme.colorScheme.outline

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
                        text = "Create account",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentDark,
                            fontSize = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Sign up to start your fitness journey",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Full Name Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Full Name",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = labelColor
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.fullName,
                            onValueChange = { viewModel.onFullNameChanged(it) },
                            placeholder = { Text("John Doe", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = uiState.fullNameError != null,
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
                        uiState.fullNameError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Email",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = labelColor
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.onEmailChanged(it) },
                            placeholder = { Text("you@example.com", color = Color.LightGray) },
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
                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = labelColor
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.onPasswordChanged(it) },
                            placeholder = { Text("••••••••", color = Color.LightGray) },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Create Account Button
                    GradientButton(
                        onClick = { viewModel.onCreateAccountClicked(onSignupSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Create Account",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.clickable { onBackToLogin() }
                    ) {
                        Text(
                            text = "Already have an account? ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray
                            )
                        )
                        Text(
                            text = "Sign in",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = accentDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
