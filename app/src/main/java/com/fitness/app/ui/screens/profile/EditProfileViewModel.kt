package com.fitness.app.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.fitness.app.auth.UserSession
import com.fitness.app.data.local.AppDatabase
import com.fitness.app.data.model.UserProfileSummaryRequest
import com.fitness.app.data.model.ProfileSummaryJson
import com.fitness.app.data.model.LastWorkout
import com.fitness.app.data.model.OneRm
import com.fitness.app.data.model.extractId
import com.fitness.app.data.model.fullName
import com.fitness.app.data.model.toUserEntity
import com.fitness.app.data.repository.AuthRepository
import com.fitness.app.data.repository.UserProfilesRepository
import com.fitness.app.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

data class EditProfileUiState(
    val password: String = "",
    val name: String = "",
    val lastName: String = "",
    val picture: String = "",
    val imageUri: String? = null,
    val sportType: String = "",
    val weeklyGoal: String = "",
    val description: String = "",
    val profileSummaryText: String = "",
    val lastWorkoutVolume: String = "",
    val lastWorkoutIntensity: String = "",
    val lastWorkoutFocusPoints: String = "",
    val lastWorkoutCaloriesBurned: String = "",
    val lastWorkoutDuration: String = "",
    val updateCount: String = "",
    val currentWeight: String = "",
    val age: String = "",
    val sex: String = "",
    val bodyFatPercentage: String = "",
    val oneRmSquat: String = "",
    val oneRmBench: String = "",
    val oneRmDeadlift: String = "",
    val workoutsPerWeek: String = "",
    val height: String = "",
    val vo2max: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class EditProfileViewModel : BaseViewModel<EditProfileUiState>(EditProfileUiState()) {
    private val authRepository = AuthRepository()
    private val userProfilesRepository = UserProfilesRepository()

    private fun loadLocalProfile(context: Context) {
        viewModelScope.launch {
            val user =
                withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(context).userDao().getUser()
                }
            if (user != null) {
                updateState { current ->
                    current.copy(
                        sportType = current.sportType.ifBlank { user.sportType.orEmpty() },
                        profileSummaryText =
                            current.profileSummaryText.ifBlank {
                                user.profileSummaryText.orEmpty()
                            },
                        lastWorkoutVolume =
                            current.lastWorkoutVolume.ifBlank {
                                user.lastWorkoutVolume?.toString().orEmpty()
                            },
                        lastWorkoutIntensity =
                            current.lastWorkoutIntensity.ifBlank {
                                user.lastWorkoutIntensity.orEmpty()
                            },
                        lastWorkoutFocusPoints =
                            current.lastWorkoutFocusPoints.ifBlank {
                                user.lastWorkoutFocusPoints.orEmpty()
                            },
                        lastWorkoutCaloriesBurned =
                            current.lastWorkoutCaloriesBurned.ifBlank {
                                user.lastWorkoutCaloriesBurned?.toString().orEmpty()
                            },
                        lastWorkoutDuration =
                            current.lastWorkoutDuration.ifBlank {
                                user.lastWorkoutDuration?.toString().orEmpty()
                            },
                        updateCount =
                            current.updateCount.ifBlank {
                                user.updateCount?.toString().orEmpty()
                            },
                        currentWeight =
                            current.currentWeight.ifBlank {
                                user.currentWeight?.toString().orEmpty()
                            },
                        age =
                            current.age.ifBlank {
                                user.age?.toString().orEmpty()
                            },
                        sex = current.sex.ifBlank { user.sex.orEmpty() },
                        bodyFatPercentage =
                            current.bodyFatPercentage.ifBlank {
                                user.bodyFatPercentage?.toString().orEmpty()
                            },
                        oneRmSquat =
                            current.oneRmSquat.ifBlank {
                                user.oneRmSquat?.toString().orEmpty()
                            },
                        oneRmBench =
                            current.oneRmBench.ifBlank {
                                user.oneRmBench?.toString().orEmpty()
                            },
                        oneRmDeadlift =
                            current.oneRmDeadlift.ifBlank {
                                user.oneRmDeadlift?.toString().orEmpty()
                            },
                        workoutsPerWeek =
                            current.workoutsPerWeek.ifBlank {
                                user.workoutsPerWeek?.toString().orEmpty()
                            },
                        height =
                            current.height.ifBlank {
                                user.height?.toString().orEmpty()
                            },
                        vo2max =
                            current.vo2max.ifBlank {
                                user.vo2max?.toString().orEmpty()
                            }
                    )
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            UserSession.name.collect { name ->
                if (!name.isNullOrBlank()) {
                    updateState { current ->
                        if (current.name.isBlank()) current.copy(name = name) else current
                    }
                }
            }
        }
        viewModelScope.launch {
            UserSession.picture.collect { picture ->
                if (!picture.isNullOrBlank()) {
                    updateState { current ->
                        if (current.picture.isBlank()) current.copy(picture = picture) else current
                    }
                }
            }
        }
        viewModelScope.launch {
            UserSession.bio.collect { bio ->
                if (!bio.isNullOrBlank()) {
                    updateState { current ->
                        if (current.description.isBlank()) current.copy(description = bio) else current
                    }
                }
            }
        }
        viewModelScope.launch {
            UserSession.sportType.collect { sportType ->
                if (!sportType.isNullOrBlank()) {
                    updateState { current ->
                        if (current.sportType.isBlank()) current.copy(sportType = sportType) else current
                    }
                }
            }
        }
        refreshProfile()
    }

    private fun refreshProfile() {
        viewModelScope.launch {
            val result =
                withContext(Dispatchers.IO) {
                    authRepository.getProfile()
                }
            result.onSuccess { profile ->
                updateState { current ->
                    current.copy(
                        name = current.name.ifBlank { profile.name.orEmpty() },
                        lastName = current.lastName.ifBlank { profile.lastName.orEmpty() },
                        picture = current.picture.ifBlank { profile.picture.orEmpty() },
                        sportType = current.sportType.ifBlank { profile.sportType.orEmpty() },
                        weeklyGoal =
                            profile.preferences?.weeklyGoal?.toString() ?: current.weeklyGoal,
                        description = current.description.ifBlank { profile.description.orEmpty() }
                    )
                }
            }
        }
    }

    fun onPasswordChanged(value: String) {
        updateState { it.copy(password = value, errorMessage = null) }
    }

    fun onNameChanged(value: String) {
        updateState { it.copy(name = value, errorMessage = null) }
    }

    fun onLastNameChanged(value: String) {
        updateState { it.copy(lastName = value, errorMessage = null) }
    }

    fun onPictureChanged(value: String) {
        updateState { it.copy(picture = value, errorMessage = null) }
    }

    fun onImageSelected(uri: Uri?) {
        updateState {
            it.copy(imageUri = uri?.toString(), errorMessage = null)
        }
    }

    fun onSportTypeChanged(value: String) {
        updateState { it.copy(sportType = value, errorMessage = null) }
    }

    fun onWeeklyGoalChanged(value: String) {
        updateState { it.copy(weeklyGoal = value, errorMessage = null) }
    }

    fun onDescriptionChanged(value: String) {
        updateState { it.copy(description = value, errorMessage = null) }
    }

    fun onProfileSummaryTextChanged(value: String) {
        updateState { it.copy(profileSummaryText = value, errorMessage = null) }
    }

    fun onLastWorkoutVolumeChanged(value: String) {
        updateState { it.copy(lastWorkoutVolume = value, errorMessage = null) }
    }

    fun onLastWorkoutIntensityChanged(value: String) {
        updateState { it.copy(lastWorkoutIntensity = value, errorMessage = null) }
    }

    fun onLastWorkoutFocusPointsChanged(value: String) {
        updateState { it.copy(lastWorkoutFocusPoints = value, errorMessage = null) }
    }

    fun onLastWorkoutCaloriesBurnedChanged(value: String) {
        updateState { it.copy(lastWorkoutCaloriesBurned = value, errorMessage = null) }
    }

    fun onLastWorkoutDurationChanged(value: String) {
        updateState { it.copy(lastWorkoutDuration = value, errorMessage = null) }
    }

    fun onUpdateCountChanged(value: String) {
        updateState { it.copy(updateCount = value, errorMessage = null) }
    }

    fun onCurrentWeightChanged(value: String) {
        updateState { it.copy(currentWeight = value, errorMessage = null) }
    }

    fun onAgeChanged(value: String) {
        updateState { it.copy(age = value, errorMessage = null) }
    }

    fun onSexChanged(value: String) {
        updateState { it.copy(sex = value, errorMessage = null) }
    }

    fun onBodyFatPercentageChanged(value: String) {
        updateState { it.copy(bodyFatPercentage = value, errorMessage = null) }
    }

    fun onOneRmSquatChanged(value: String) {
        updateState { it.copy(oneRmSquat = value, errorMessage = null) }
    }

    fun onOneRmBenchChanged(value: String) {
        updateState { it.copy(oneRmBench = value, errorMessage = null) }
    }

    fun onOneRmDeadliftChanged(value: String) {
        updateState { it.copy(oneRmDeadlift = value, errorMessage = null) }
    }

    fun onWorkoutsPerWeekChanged(value: String) {
        updateState { it.copy(workoutsPerWeek = value, errorMessage = null) }
    }

    fun onHeightChanged(value: String) {
        updateState { it.copy(height = value, errorMessage = null) }
    }

    fun onVo2maxChanged(value: String) {
        updateState { it.copy(vo2max = value, errorMessage = null) }
    }

    fun submit(context: Context, onSuccess: () -> Unit) {
        val weeklyGoalText = uiState.value.weeklyGoal.trim()
        val weeklyGoal =
            if (weeklyGoalText.isBlank()) null
            else weeklyGoalText.toIntOrNull()

        if (weeklyGoalText.isNotBlank() && weeklyGoal == null) {
            updateState { it.copy(errorMessage = "Weekly goal must be a number") }
            return
        }

        updateState { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val imageUri = uiState.value.imageUri?.let { Uri.parse(it) }
            val result =
                withContext(Dispatchers.IO) {
                    val filePart =
                        if (imageUri != null) {
                            createFilePart(context, imageUri)
                        } else {
                            null
                        }
                    if (imageUri != null && filePart == null) {
                        return@withContext Result.failure(
                            Exception("Unable to read selected image")
                        )
                    }
                    authRepository.updateProfileWithImage(
                        file = filePart,
                        password = textPart(uiState.value.password),
                        name = textPart(uiState.value.name),
                        lastName = textPart(uiState.value.lastName),
                        sportType = textPart(uiState.value.sportType),
                        description = textPart(uiState.value.description),
                        weeklyGoal = weeklyGoal?.let { goal -> textPart(goal.toString()) }
                    )
                }

            result.fold(
                onSuccess = { profile ->
                    val userId = UserSession.userId.value ?: profile.extractId()
                    val extraPayload = buildUserProfileSummaryPayload(userId)
                    if (extraPayload == null && hasExtraProfileFields()) {
                        updateState {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Missing user id for profile summary"
                            )
                        }
                        return@fold
                    }
                    val extraResult =
                        if (extraPayload != null) {
                            userProfilesRepository.upsertUserProfile(extraPayload)
                        } else {
                            Result.success(Unit)
                        }
                    if (extraResult.isFailure) {
                        updateState {
                            it.copy(
                                isLoading = false,
                                errorMessage = extraResult.exceptionOrNull()?.message
                            )
                        }
                        return@fold
                    }

                    withContext(Dispatchers.IO) {
                        val dao = AppDatabase.getInstance(context).userDao()
                        val entity = profile.toUserEntity()
                        val updated =
                            if (extraPayload != null) {
                                entity.copy(
                                    profileSummaryText = extraPayload.profileSummaryText,
                                    lastWorkoutVolume =
                                        extraPayload.profileSummaryJson?.lastWorkout?.volume,
                                    lastWorkoutIntensity =
                                        extraPayload.profileSummaryJson?.lastWorkout?.intensity,
                                    lastWorkoutFocusPoints =
                                        extraPayload.profileSummaryJson?.lastWorkout?.focusPoints
                                            ?.joinToString(","),
                                    lastWorkoutCaloriesBurned =
                                        extraPayload.profileSummaryJson?.lastWorkout?.caloriesBurned,
                                    lastWorkoutDuration =
                                        extraPayload.profileSummaryJson?.lastWorkout?.duration,
                                    updateCount = extraPayload.profileSummaryJson?.updateCount,
                                    currentWeight = extraPayload.currentWeight,
                                    age = extraPayload.age,
                                    sex = extraPayload.sex,
                                    bodyFatPercentage = extraPayload.bodyFatPercentage,
                                    oneRmSquat = extraPayload.oneRm?.squat,
                                    oneRmBench = extraPayload.oneRm?.bench,
                                    oneRmDeadlift = extraPayload.oneRm?.deadlift,
                                    workoutsPerWeek = extraPayload.workoutsPerWeek,
                                    height = extraPayload.height,
                                    vo2max = extraPayload.vo2max
                                )
                            } else {
                                entity
                            }
                        dao.upsert(updated)
                        UserSession.setUser(
                            userId = profile.extractId(),
                            name = profile.fullName(),
                            username = entity.username,
                            email = profile.email,
                            picture = profile.picture,
                            bio = profile.description,
                            sportType = profile.sportType,
                            streak = profile.streak
                        )
                    }
                    updateState { it.copy(isLoading = false, errorMessage = null) }
                    onSuccess()
                },
                onFailure = { error ->
                    updateState { it.copy(isLoading = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun loadLocalDefaults(context: Context) {
        loadLocalProfile(context)
    }

    private fun textPart(value: String?): RequestBody? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isBlank()) return null
        return trimmed.toRequestBody("text/plain".toMediaType())
    }

    private fun parseIntOrNull(value: String): Int? {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return null
        return trimmed.toIntOrNull()
    }

    private fun parseFocusPoints(value: String): List<String>? {
        val parts =
            value.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        return parts.takeIf { it.isNotEmpty() }
    }

    private fun buildUserProfileSummaryPayload(userId: String?): UserProfileSummaryRequest? {
        if (userId.isNullOrBlank()) return null
        val state = uiState.value

        val lastWorkout =
            LastWorkout(
                volume = parseIntOrNull(state.lastWorkoutVolume),
                intensity = state.lastWorkoutIntensity.trim().ifBlank { null },
                focusPoints = parseFocusPoints(state.lastWorkoutFocusPoints),
                caloriesBurned = parseIntOrNull(state.lastWorkoutCaloriesBurned),
                duration = parseIntOrNull(state.lastWorkoutDuration)
            ).takeIf { lw ->
                lw.volume != null ||
                    !lw.intensity.isNullOrBlank() ||
                    !lw.focusPoints.isNullOrEmpty() ||
                    lw.caloriesBurned != null ||
                    lw.duration != null
            }

        val summaryJson =
            ProfileSummaryJson(
                lastWorkout = lastWorkout,
                updateCount = parseIntOrNull(state.updateCount)
            ).takeIf { sj ->
                sj.lastWorkout != null || sj.updateCount != null
            }

        val oneRm =
            OneRm(
                squat = parseIntOrNull(state.oneRmSquat),
                bench = parseIntOrNull(state.oneRmBench),
                deadlift = parseIntOrNull(state.oneRmDeadlift)
            ).takeIf { rm ->
                rm.squat != null || rm.bench != null || rm.deadlift != null
            }

        val payload =
            UserProfileSummaryRequest(
                userId = userId,
                profileSummaryText = state.profileSummaryText.trim().ifBlank { null },
                profileSummaryJson = summaryJson,
                currentWeight = parseIntOrNull(state.currentWeight),
                age = parseIntOrNull(state.age),
                sex = state.sex.trim().ifBlank { null },
                bodyFatPercentage = parseIntOrNull(state.bodyFatPercentage),
                oneRm = oneRm,
                workoutsPerWeek = parseIntOrNull(state.workoutsPerWeek),
                height = parseIntOrNull(state.height),
                vo2max = parseIntOrNull(state.vo2max)
            )

        val hasAny =
            payload.profileSummaryText != null ||
                payload.profileSummaryJson != null ||
                payload.currentWeight != null ||
                payload.age != null ||
                payload.sex != null ||
                payload.bodyFatPercentage != null ||
                payload.oneRm != null ||
                payload.workoutsPerWeek != null ||
                payload.height != null ||
                payload.vo2max != null

        return if (hasAny) payload else null
    }

    private fun hasExtraProfileFields(): Boolean {
        val state = uiState.value
        return state.profileSummaryText.isNotBlank() ||
            state.lastWorkoutVolume.isNotBlank() ||
            state.lastWorkoutIntensity.isNotBlank() ||
            state.lastWorkoutFocusPoints.isNotBlank() ||
            state.lastWorkoutCaloriesBurned.isNotBlank() ||
            state.lastWorkoutDuration.isNotBlank() ||
            state.updateCount.isNotBlank() ||
            state.currentWeight.isNotBlank() ||
            state.age.isNotBlank() ||
            state.sex.isNotBlank() ||
            state.bodyFatPercentage.isNotBlank() ||
            state.oneRmSquat.isNotBlank() ||
            state.oneRmBench.isNotBlank() ||
            state.oneRmDeadlift.isNotBlank() ||
            state.workoutsPerWeek.isNotBlank() ||
            state.height.isNotBlank() ||
            state.vo2max.isNotBlank()
    }

    private fun createFilePart(context: Context, uri: Uri): MultipartBody.Part? {
        return try {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(uri) ?: "image/*"
            val name = queryFileName(context, uri) ?: "avatar.jpg"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val body = bytes.toRequestBody(mimeType.toMediaType())
            MultipartBody.Part.createFormData("file", name, body)
        } catch (_: Exception) {
            null
        }
    }

    private fun queryFileName(context: Context, uri: Uri): String? {
        val resolver = context.contentResolver
        val cursor =
            resolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                ?: return null
        cursor.use {
            return if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0) it.getString(index) else null
            } else {
                null
            }
        }
    }
}
