package com.fitness.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fitness.app.ui.screens.main.MainScreen
import com.fitness.app.ui.theme.FitnessAppTheme
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import androidx.fragment.app.activityViewModels

class MainFragment : Fragment() {
    private val cameraViewModel: com.fitness.app.ui.viewmodels.CameraViewModel by activityViewModels()
    private var pendingImageUri: android.net.Uri? = null

    private val takePictureLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraViewModel.onCameraResult(pendingImageUri)
        } else {
            cameraViewModel.onCameraResult(null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FitnessAppTheme {
                    MainScreen(
                        onLogout = {
                            findNavController().navigate(MainFragmentDirections.actionMainFragmentToLoginFragment())
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                cameraViewModel.shouldLaunchCamera.collect { shouldLaunch ->
                    if (shouldLaunch) {
                        launchCamera()
                        cameraViewModel.clearCameraRequest()
                    }
                }
            }
        }
    }

    private fun launchCamera() {
        val uri = createTempImageUri()
        if (uri != null) {
            pendingImageUri = uri
            takePictureLauncher.launch(uri)
        } else {
            android.util.Log.e("MainFragment", "Failed to create temp image URI")
        }
    }

    private fun createTempImageUri(): android.net.Uri? {
        return try {
            val context = requireContext()
            val imageDir = java.io.File(context.cacheDir, "images").apply { mkdirs() }
            val fileName = "post_${java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())}.jpg"
            val imageFile = java.io.File(imageDir, fileName)
            androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
