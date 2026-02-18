package com.fitness.app.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraViewModel : ViewModel() {
    private val _cameraImageUri = MutableStateFlow<Uri?>(null)
    val cameraImageUri: StateFlow<Uri?> = _cameraImageUri.asStateFlow()

    private val _shouldLaunchCamera = MutableStateFlow(false)
    val shouldLaunchCamera: StateFlow<Boolean> = _shouldLaunchCamera.asStateFlow()

    fun onCameraResult(uri: Uri?) {
        _cameraImageUri.value = uri
        _shouldLaunchCamera.value = false
    }

    fun requestCameraLaunch() {
        _shouldLaunchCamera.value = true
    }
    
    fun clearCameraRequest() {
        _shouldLaunchCamera.value = false
    }
}
