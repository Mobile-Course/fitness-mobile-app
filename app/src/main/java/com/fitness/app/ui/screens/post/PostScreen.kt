package com.fitness.app.ui.screens.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.fitness.app.ui.components.CMSDropdown
import com.fitness.app.ui.components.CMSInputField
import com.fitness.app.ui.components.DismissibleErrorBanner
import com.fitness.app.ui.components.PhotoSelector

/**
 * Composable representing the Post Creation Page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    onPostCreated: () -> Unit,
    viewModel: PostViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val bgColor = Color(0xFFF0F4F8)
    val accentDark = Color(0xFF343E4E)

    // Using Scaffold for TopAppBar
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Post",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentDark
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Handle back navigation if needed */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = accentDark
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.submitPost(onSuccess = onPostCreated) },
                        colors = ButtonDefaults.buttonColors(containerColor = accentDark),
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        // Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        // Spacer(modifier = Modifier.width(8.dp))
                        Text("Share")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Divider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(24.dp))

            // Error Banner
            uiState.error?.let {
                DismissibleErrorBanner(
                    error = it,
                    onDismiss = { viewModel.clearError() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Title
            CMSInputField(
                label = "Title",
                value = uiState.title,
                onValueChange = { viewModel.onTitleChanged(it) },
                placeholder = "What's your workout about?"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            CMSInputField(
                label = "Description (optional)",
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                placeholder = "Share your workout experience, progress, or motivation...",
                minLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Add Photo
            val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia(),
                onResult = { uri -> viewModel.onImageSelected(uri) }
            )

            PhotoSelector(
                onClick = {
                    singlePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                selectedImageUri = uiState.selectedImageUri
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Workout Details Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Workout Details (Optional)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = accentDark
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Workout Type
                    CMSDropdown(
                        label = "Workout Type",
                        options = listOf("Cardio", "Strength", "Yoga", "HIIT", "Other"),
                        selectedOption = uiState.workoutType,
                        onOptionSelected = { viewModel.onWorkoutTypeChanged(it) },
                        placeholder = "Select workout type"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Duration & Calories Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CMSInputField(
                            label = "Duration (min)",
                            value = uiState.duration,
                            onValueChange = { viewModel.onDurationChanged(it) },
                            placeholder = "45",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        CMSInputField(
                            label = "Calories Burned",
                            value = uiState.calories,
                            onValueChange = { viewModel.onCaloriesChanged(it) },
                            placeholder = "350",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
