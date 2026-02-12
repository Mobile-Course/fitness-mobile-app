package com.fitness.app.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.fitness.app.auth.UserSession
import com.fitness.app.data.local.AppDatabase
import com.fitness.app.data.model.OneRm
import com.fitness.app.data.model.UserProfileSummaryRequest
import com.fitness.app.data.model.extractId
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
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

class EditProfileViewModel : BaseViewModel<EditProfileUiState>(EditProfileUiState()) {
    private val authRepository = AuthRepository()
    private val userProfilesRepository = UserProfilesRepository()

    companion object {
        private const val FIELD_NAME = "name"
        private const val FIELD_LAST_NAME = "lastName"
        private const val FIELD_PASSWORD = "password"
        private const val FIELD_SPORT_TYPE = "sportType"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_WORKOUTS_PER_WEEK = "workoutsPerWeek"
        private const val FIELD_AGE = "age"
        private const val FIELD_HEIGHT = "height"
        private const val FIELD_WEIGHT = "currentWeight"
        private const val FIELD_SEX = "sex"
        private const val FIELD_BODY_FAT = "bodyFatPercentage"
        private const val FIELD_VO2MAX = "vo2max"
        private const val FIELD_SQUAT = "oneRmSquat"
        private const val FIELD_BENCH = "oneRmBench"
        private const val FIELD_DEADLIFT = "oneRmDeadlift"

        private val SPORT_TYPES =
            setOf(
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
    }

    private fun updateField(
        field: String,
        reducer: (EditProfileUiState) -> EditProfileUiState
    ) {
        updateState { current ->
            val next = reducer(current)
            next.copy(
                errorMessage = null,
                fieldErrors = current.fieldErrors - field
            )
        }
    }

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
        updateField(FIELD_PASSWORD) { it.copy(password = value) }
    }

    fun onNameChanged(value: String) {
        updateField(FIELD_NAME) { it.copy(name = value) }
    }

    fun onLastNameChanged(value: String) {
        updateField(FIELD_LAST_NAME) { it.copy(lastName = value) }
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
        updateField(FIELD_SPORT_TYPE) { it.copy(sportType = value) }
    }

    fun onWeeklyGoalChanged(value: String) {
        updateState { it.copy(weeklyGoal = value, errorMessage = null) }
    }

    fun onDescriptionChanged(value: String) {
        updateField(FIELD_DESCRIPTION) { it.copy(description = value) }
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
        updateField(FIELD_WEIGHT) { it.copy(currentWeight = value) }
    }

    fun onAgeChanged(value: String) {
        updateField(FIELD_AGE) { it.copy(age = value) }
    }

    fun onSexChanged(value: String) {
        updateField(FIELD_SEX) { it.copy(sex = value) }
    }

    fun onBodyFatPercentageChanged(value: String) {
        updateField(FIELD_BODY_FAT) { it.copy(bodyFatPercentage = value) }
    }

    fun onOneRmSquatChanged(value: String) {
        updateField(FIELD_SQUAT) { it.copy(oneRmSquat = value) }
    }

    fun onOneRmBenchChanged(value: String) {
        updateField(FIELD_BENCH) { it.copy(oneRmBench = value) }
    }

    fun onOneRmDeadliftChanged(value: String) {
        updateField(FIELD_DEADLIFT) { it.copy(oneRmDeadlift = value) }
    }

    fun onWorkoutsPerWeekChanged(value: String) {
        updateField(FIELD_WORKOUTS_PER_WEEK) { it.copy(workoutsPerWeek = value) }
    }

    fun onHeightChanged(value: String) {
        updateField(FIELD_HEIGHT) { it.copy(height = value) }
    }

    fun onVo2maxChanged(value: String) {
        updateField(FIELD_VO2MAX) { it.copy(vo2max = value) }
    }

    fun submit(context: Context, onSuccess: () -> Unit) {
        val validationErrors = validateForSubmit(uiState.value)
        if (validationErrors.isNotEmpty()) {
            updateState {
                it.copy(
                    fieldErrors = validationErrors,
                    errorMessage = "Please fix the highlighted fields"
                )
            }
            return
        }

        val weeklyGoalText = uiState.value.weeklyGoal.trim()
        val weeklyGoal =
            if (weeklyGoalText.isBlank()) null
            else weeklyGoalText.toIntOrNull()

        if (weeklyGoalText.isNotBlank() && weeklyGoal == null) {
            updateState { it.copy(errorMessage = "Weekly goal must be a number") }
            return
        }

        updateState { it.copy(isLoading = true, errorMessage = null, fieldErrors = emptyMap()) }
        viewModelScope.launch {
            val imageUri = uiState.value.imageUri?.let { Uri.parse(it) }
            val hasAuthChanges =
                imageUri != null ||
                    uiState.value.password.isNotBlank() ||
                    uiState.value.name.isNotBlank() ||
                    uiState.value.lastName.isNotBlank() ||
                    uiState.value.sportType.isNotBlank() ||
                    uiState.value.description.isNotBlank() ||
                    weeklyGoal != null
            val result =
                withContext(Dispatchers.IO) {
                    if (hasAuthChanges) {
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
                    } else {
                        authRepository.getProfile()
                    }
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
                        val previous = dao.getUser()
                        val entity = profile.toUserEntity()
                        val updated =
                            if (extraPayload != null) {
                                entity.copy(
                                    profileSummaryText = previous?.profileSummaryText,
                                    lastWorkoutVolume = previous?.lastWorkoutVolume,
                                    lastWorkoutIntensity = previous?.lastWorkoutIntensity,
                                    lastWorkoutFocusPoints = previous?.lastWorkoutFocusPoints,
                                    lastWorkoutCaloriesBurned = previous?.lastWorkoutCaloriesBurned,
                                    lastWorkoutDuration = previous?.lastWorkoutDuration,
                                    updateCount = previous?.updateCount,
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
                            name = profile.name,
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

    private fun parseOptionalInt(
        value: String,
        field: String,
        label: String,
        min: Int,
        max: Int,
        errors: MutableMap<String, String>
    ): Int? {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return null
        val parsed = trimmed.toIntOrNull()
        if (parsed == null) {
            errors[field] = "$label must be a whole number"
            return null
        }
        if (parsed < min || parsed > max) {
            errors[field] = "$label must be between $min and $max"
            return null
        }
        return parsed
    }

    private fun validateForSubmit(state: EditProfileUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        val name = state.name.trim()
        if (name.isNotEmpty() && !Regex("^[A-Za-z .'-]{2,40}$").matches(name)) {
            errors[FIELD_NAME] = "Name must be 2-40 letters and valid separators"
        }

        val lastName = state.lastName.trim()
        if (lastName.isNotEmpty() && !Regex("^[A-Za-z .'-]{2,40}$").matches(lastName)) {
            errors[FIELD_LAST_NAME] = "Last name must be 2-40 letters and valid separators"
        }

        val password = state.password.trim()
        if (password.isNotEmpty() && password.length < 6) {
            errors[FIELD_PASSWORD] = "Password must be at least 6 characters"
        }

        val sportType = state.sportType.trim()
        if (sportType.isNotEmpty() && SPORT_TYPES.none { it.equals(sportType, ignoreCase = true) }) {
            errors[FIELD_SPORT_TYPE] = "Select a valid sport type"
        }

        if (state.description.length > 500) {
            errors[FIELD_DESCRIPTION] = "Description cannot exceed 500 characters"
        }

        parseOptionalInt(state.workoutsPerWeek, FIELD_WORKOUTS_PER_WEEK, "Workouts per week", 0, 14, errors)
        parseOptionalInt(state.age, FIELD_AGE, "Age", 10, 120, errors)
        parseOptionalInt(state.height, FIELD_HEIGHT, "Height", 50, 260, errors)
        parseOptionalInt(state.currentWeight, FIELD_WEIGHT, "Weight", 20, 400, errors)
        parseOptionalInt(state.bodyFatPercentage, FIELD_BODY_FAT, "Body fat", 1, 70, errors)
        parseOptionalInt(state.vo2max, FIELD_VO2MAX, "VO2 max", 10, 100, errors)
        parseOptionalInt(state.oneRmSquat, FIELD_SQUAT, "Squat 1RM", 1, 600, errors)
        parseOptionalInt(state.oneRmBench, FIELD_BENCH, "Bench 1RM", 1, 500, errors)
        parseOptionalInt(state.oneRmDeadlift, FIELD_DEADLIFT, "Deadlift 1RM", 1, 700, errors)

        val sex = state.sex.trim()
        if (sex.isNotEmpty() && sex.lowercase() !in setOf("male", "female", "other")) {
            errors[FIELD_SEX] = "Sex must be Male, Female, or Other"
        }

        return errors
    }

    private fun buildUserProfileSummaryPayload(userId: String?): UserProfileSummaryRequest? {
        if (userId.isNullOrBlank()) return null
        val state = uiState.value

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
                currentWeight = parseIntOrNull(state.currentWeight),
                age = parseIntOrNull(state.age),
                sex = state.sex.trim().lowercase().ifBlank { null },
                bodyFatPercentage = parseIntOrNull(state.bodyFatPercentage),
                oneRm = oneRm,
                workoutsPerWeek = parseIntOrNull(state.workoutsPerWeek),
                height = parseIntOrNull(state.height),
                vo2max = parseIntOrNull(state.vo2max)
            )

        val hasAny =
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
        return state.currentWeight.isNotBlank() ||
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
