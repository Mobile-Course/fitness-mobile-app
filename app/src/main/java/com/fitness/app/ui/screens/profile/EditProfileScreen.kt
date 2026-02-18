package com.fitness.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.fitness.app.ui.components.FitTrackHeader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onProfileUpdated: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiStateLiveData.observeAsState(EditProfileUiState())
    val fieldErrors = uiState.fieldErrors
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val bgColor = Color(0xFFF0F4F8)
    val accentDark = Color(0xFF343E4E)
    val sportTypeOptions =
        listOf(
            "Athlete",
            "Runner",
            "Cyclist",
            "Swimmer",
            "Weightlifter",
            "Bodybuilder",
            "CrossFit",
            "Yoga Practitioner",
            "Martial Artist",
            "Climber",
            "Dancer",
            "FitnessEnthusiast"
        )
    var sportTypeExpanded by rememberSaveable { mutableStateOf(false) }
    val sexOptions = listOf("Male", "Female", "Other")
    var sexExpanded by rememberSaveable { mutableStateOf(false) }
    val imagePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            viewModel.onImageSelected(uri)
        }

    LaunchedEffect(Unit) {
        viewModel.loadLocalDefaults(context)
    }

    Scaffold(
        topBar = {
            FitTrackHeader(
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = accentDark
                        )
                    }
                }
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = innerPadding.calculateTopPadding() + 8.dp,
                            bottom = 24.dp
                        )
                    ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            fun errorFor(field: String): String? = fieldErrors[field]
            val selectedUri =
                uiState.imageUri?.let { Uri.parse(it) }
            val previewModel =
                selectedUri
                    ?: uiState.picture.takeIf { it.isNotBlank() }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(previewModel)
                            .crossfade(true)
                            .build(),
                    contentDescription = "Profile picture",
                    modifier = Modifier.size(72.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Choose Photo")
                }
            }

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChanged,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorFor("name") != null,
                supportingText = {
                    val error = errorFor("name")
                    if (error != null) Text(error)
                }
            )
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = errorFor("password") != null,
                supportingText = {
                    val error = errorFor("password")
                    if (error != null) Text(error)
                }
            )
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.sportType,
                    onValueChange = { },
                    label = { Text("Sport Type") },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { sportTypeExpanded = true },
                    readOnly = true,
                    singleLine = true,
                    isError = errorFor("sportType") != null,
                    supportingText = {
                        val error = errorFor("sportType")
                        if (error != null) Text(error)
                    },
                    trailingIcon = {
                        IconButton(onClick = { sportTypeExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = sportTypeExpanded,
                    onDismissRequest = { sportTypeExpanded = false }
                ) {
                    sportTypeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.onSportTypeChanged(option)
                                sportTypeExpanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = uiState.workoutsPerWeek,
                onValueChange = viewModel::onWorkoutsPerWeekChanged,
                label = { Text("Workouts Per Week") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = errorFor("workoutsPerWeek") != null,
                supportingText = {
                    val error = errorFor("workoutsPerWeek")
                    if (error != null) Text(error)
                }
            )
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                isError = errorFor("description") != null,
                supportingText = {
                    val error = errorFor("description")
                    if (error != null) Text(error)
                }
            )

            Text(
                text = "Basics",
                style = MaterialTheme.typography.titleMedium,
                color = accentDark
            )

            // Row 1: Age, Height, Weight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.age,
                    onValueChange = viewModel::onAgeChanged,
                    label = { Text("Age", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorFor("age") != null,
                    supportingText = {
                        val error = errorFor("age")
                        if (error != null) Text(error)
                    }
                )
                OutlinedTextField(
                    value = uiState.height,
                    onValueChange = viewModel::onHeightChanged,
                    label = { Text("Height", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorFor("height") != null,
                    supportingText = {
                        val error = errorFor("height")
                        if (error != null) Text(error)
                    }
                )
                OutlinedTextField(
                    value = uiState.currentWeight,
                    onValueChange = viewModel::onCurrentWeightChanged,
                    label = { Text("Weight", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorFor("currentWeight") != null,
                    supportingText = {
                        val error = errorFor("currentWeight")
                        if (error != null) Text(error)
                    }
                )
            }

            // Row 2: Sex, Body Fat %, VO2max
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = uiState.sex,
                        onValueChange = { },
                        label = { Text("Sex", style = MaterialTheme.typography.labelSmall) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { sexExpanded = true },
                        readOnly = true,
                        singleLine = true,
                        isError = errorFor("sex") != null,
                        supportingText = {
                            val error = errorFor("sex")
                            if (error != null) Text(error)
                        },
                        trailingIcon = {
                            IconButton(onClick = { sexExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = sexExpanded,
                        onDismissRequest = { sexExpanded = false }
                    ) {
                        sexOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.onSexChanged(option)
                                    sexExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = uiState.bodyFatPercentage,
                    onValueChange = viewModel::onBodyFatPercentageChanged,
                    label = { Text("Fat %", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorFor("bodyFatPercentage") != null,
                    supportingText = {
                        val error = errorFor("bodyFatPercentage")
                        if (error != null) Text(error)
                    }
                )
                OutlinedTextField(
                    value = uiState.vo2max,
                    onValueChange = viewModel::onVo2maxChanged,
                    label = { Text("VO2max", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorFor("vo2max") != null,
                    supportingText = {
                        val error = errorFor("vo2max")
                        if (error != null) Text(error)
                    }
                )
            }

            Text(
                text = "One Rep Max (1RM)",
                style = MaterialTheme.typography.titleMedium,
                color = accentDark
            )

            // Row 3: Squat, Bench, Deadlift
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.oneRmSquat,
                    onValueChange = viewModel::onOneRmSquatChanged,
                    label = { Text("Squat", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorFor("oneRmSquat") != null,
                    supportingText = {
                        val error = errorFor("oneRmSquat")
                        if (error != null) Text(error)
                    }
                )
                OutlinedTextField(
                    value = uiState.oneRmBench,
                    onValueChange = viewModel::onOneRmBenchChanged,
                    label = { Text("Bench", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorFor("oneRmBench") != null,
                    supportingText = {
                        val error = errorFor("oneRmBench")
                        if (error != null) Text(error)
                    }
                )
                OutlinedTextField(
                    value = uiState.oneRmDeadlift,
                    onValueChange = viewModel::onOneRmDeadliftChanged,
                    label = { Text("Deadlift", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorFor("oneRmDeadlift") != null,
                    supportingText = {
                        val error = errorFor("oneRmDeadlift")
                        if (error != null) Text(error)
                    }
                )
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.submit(context, onProfileUpdated) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = accentDark)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isLoading) "Saving..." else "Save Changes"
                )
            }
        }
    }
}
