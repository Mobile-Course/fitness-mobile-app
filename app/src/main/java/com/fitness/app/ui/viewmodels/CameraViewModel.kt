package com.fitness.app.ui.viewmodels

import android.net.Uri
import com.fitness.app.ui.base.BaseViewModel

data class CameraUiState(
    val cameraImageUri: Uri? = null,
    val shouldLaunchCamera: Boolean = false
)

class CameraViewModel : BaseViewModel<CameraUiState>(CameraUiState()) {
    fun onCameraResult(uri: Uri?) {
        updateState { it.copy(cameraImageUri = uri, shouldLaunchCamera = false) }
    }

    fun requestCameraLaunch() {
        updateState { it.copy(shouldLaunchCamera = true) }
    }
    
    fun clearCameraRequest() {
        updateState { it.copy(shouldLaunchCamera = false) }
    }
}
