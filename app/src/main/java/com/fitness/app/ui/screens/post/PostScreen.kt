package com.fitness.app.ui.screens.post

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitness.app.ui.components.CMSDropdown
import com.fitness.app.ui.components.CMSInputField
import com.fitness.app.ui.components.DismissibleErrorBanner
import com.fitness.app.ui.components.FitTrackHeader
import com.fitness.app.ui.components.GradientButton
import com.fitness.app.ui.components.PhotoSelector
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.FileProvider

import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    onPostCreated: () -> Unit,
    onCancel: () -> Unit = {},
    postId: String? = null,
    postViewModel: PostViewModel = viewModel()
) {
    val uiState by postViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(postId) {
        if (postId != null) {
            postViewModel.loadPost(postId)
        } else {
             if (uiState.isEditing) postViewModel.resetForm()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF0F4F8),
        topBar = {
            FitTrackHeader(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.currentStep == 1) {
                                postViewModel.resetForm()
                                onCancel()
                            } else {
                                postViewModel.previousStep()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Divider(color = Color(0xFFE2E8F0))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.currentStep == 1) {
                    TextButton(
                        onClick = {
                            postViewModel.resetForm()
                            onCancel()
                        }
                    ) {
                        Text("Cancel", color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                    }
                    GradientButton(
                        onClick = { postViewModel.nextStep() },
                        modifier = Modifier.height(44.dp),
                        enabled = !uiState.isPosting
                    ) {
                        Text("Next Step", fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = { postViewModel.previousStep() }) {
                        Text("Back", color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                    }
                    GradientButton(
                        onClick = {
                            postViewModel.submitPost(context = context, onSuccess = onPostCreated)
                        },
                        modifier = Modifier.height(44.dp),
                        enabled = !uiState.isPosting
                    ) {
                        if (uiState.isPosting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (uiState.isEditing) "Update Post" else "Share Post", fontWeight = FontWeight.Bold)
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Stepper(currentStep = uiState.currentStep)
            Spacer(modifier = Modifier.height(20.dp))

            uiState.error?.let {
                DismissibleErrorBanner(error = it, onDismiss = { postViewModel.clearError() })
                Spacer(modifier = Modifier.height(12.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.currentStep == 1) {
                        BasicsStep(
                            uiState = uiState,
                            onTitleChanged = { postViewModel.onTitleChanged(it) },
                            onDescriptionChanged = { postViewModel.onDescriptionChanged(it) },
                            onImageSelected = { postViewModel.onImageSelected(it) }
                        )
                    } else {
                        DetailsStep(
                            uiState = uiState,
                            onWorkoutTypeChanged = { postViewModel.onWorkoutTypeChanged(it) },
                            onDurationChanged = { postViewModel.onDurationChanged(it) },
                            onCaloriesChanged = { postViewModel.onCaloriesChanged(it) },
                            onSubjectiveFeedbackFeelingsChanged = { postViewModel.onSubjectiveFeedbackFeelingsChanged(it) },
                            onPersonalGoalsChanged = { postViewModel.onPersonalGoalsChanged(it) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicsStep(
    uiState: PostUiState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onImageSelected: (android.net.Uri?) -> Unit
) {
    val context = LocalContext.current
    var showPhotoSourceSheet by remember { mutableStateOf(false) }

    // Obtain CameraViewModel scoped to the Activity to share with MainFragment
    val cameraViewModel: com.fitness.app.ui.viewmodels.CameraViewModel = viewModel(
        viewModelStoreOwner = context as androidx.activity.ComponentActivity
    )
    val cameraImageUri by cameraViewModel.cameraImageUri.collectAsState()

    // Consume the camera result
    LaunchedEffect(cameraImageUri) {
        cameraImageUri?.let { uri ->
            onImageSelected(uri)
            cameraViewModel.onCameraResult(null) // Clear after consumption
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val singlePhotoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = onImageSelected
        )
    // Gallery picker launcher (kept here as it's fine in Composable)

    Text(
        text = if (uiState.isEditing) "Edit Post" else "Basics",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
    )
    Spacer(modifier = Modifier.height(16.dp))
    CMSInputField(
        label = "Title",
        value = uiState.title,
        onValueChange = onTitleChanged,
        placeholder = "What's your workout about?"
    )
    Spacer(modifier = Modifier.height(16.dp))
    CMSInputField(
        label = "Description (Will help AI Tips)",
        value = uiState.description,
        onValueChange = onDescriptionChanged,
        placeholder = "Share your workout experience, progress, or motivation...",
        minLines = 5
    )
    Spacer(modifier = Modifier.height(16.dp))
    PhotoSelector(
        onClick = {
            showPhotoSourceSheet = true
        },
        selectedImageUri = uiState.selectedImageUri,
        onRemoveClick = if (uiState.selectedImageUri != null || (uiState.existingImageUrl != null && uiState.existingImageUrl.isNotBlank())) {
            { onImageSelected(null) }
        } else null,
        placeholderHeight = 96.dp,
        selectedImageHeight = 180.dp
    )

    if (showPhotoSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSourceSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Add Photo",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        showPhotoSourceSheet = false
                        // Trigger Camera via SharedViewModel
                        cameraViewModel.requestCameraLaunch()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Take Photo")
                }

                TextButton(
                    onClick = {
                        showPhotoSourceSheet = false
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choose from Gallery")
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}


@Composable
private fun DetailsStep(
    uiState: PostUiState,
    onWorkoutTypeChanged: (String) -> Unit,
    onDurationChanged: (String) -> Unit,
    onCaloriesChanged: (String) -> Unit,
    onSubjectiveFeedbackFeelingsChanged: (String) -> Unit,
    onPersonalGoalsChanged: (String) -> Unit
) {
    Text(
        text = "Workout Details (Will help AI Tips)",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
    )
    Spacer(modifier = Modifier.height(16.dp))

    CMSDropdown(
        label = "Workout Type",
        options =
            listOf(
                "Strength",
                "Cardio",
                "Running",
                "Cycling",
                "HIIT",
                "Yoga",
                "Pilates",
                "Other"
            ),
        selectedOption = uiState.workoutType,
        onOptionSelected = onWorkoutTypeChanged,
        placeholder = "Select workout type"
    )

    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CMSInputField(
            label = "Duration (min)",
            value = uiState.duration,
            onValueChange = onDurationChanged,
            placeholder = "45",
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        CMSInputField(
            label = "Calories Burned",
            value = uiState.calories,
            onValueChange = onCaloriesChanged,
            placeholder = "350",
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    CMSInputField(
        label = "Subjective feedback & feelings (won't appear in feed)",
        value = uiState.subjectiveFeedbackFeelings,
        onValueChange = onSubjectiveFeedbackFeelingsChanged,
        placeholder = "How did you feel? Energy, mood, soreness, progress...",
        minLines = 4
    )

    Spacer(modifier = Modifier.height(12.dp))
    CMSInputField(
        label = "Personal goals (won't appear in feed)",
        value = uiState.personalGoals,
        onValueChange = onPersonalGoalsChanged,
        placeholder = "Select or type a goal..."
    )
}

@Composable
private fun Stepper(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepNode(
            number = "1",
            title = "Basics",
            active = currentStep == 1,
            completed = currentStep > 1
        )
        Box(modifier = Modifier.weight(1f).height(1.dp).padding(horizontal = 8.dp)) {
            Divider(color = Color(0xFFD1D5DB))
        }
        StepNode(number = "2", title = "Details", active = currentStep == 2, completed = false)
    }
}

@Composable
private fun StepNode(number: String, title: String, active: Boolean, completed: Boolean) {
    val nodeColor =
        when {
            active -> Color(0xFF6366F1)
            completed -> Color(0xFF6366F1)
            else -> Color(0xFFD1D5DB)
        }
    val textColor = if (active || completed) Color(0xFF6366F1) else Color(0xFF6B7280)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(24.dp).background(nodeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    text = number,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        Text(
            text = title,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}
