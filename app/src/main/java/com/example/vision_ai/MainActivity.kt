package com.example.vision_ai

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.vision_ai.ui.theme.VisionAiTheme
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageCapture

import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.example.vision_ai.camera.CameraPreviewScreen
import com.example.vision_ai.camera.CameraView
import com.example.vision_ai.camera.viewModel.CameraViewModel
import com.example.vision_ai.voice_to_text.presentation.model.VoiceToTextEvent
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val cameraPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                setCameraPreview()
            } else {
                // Camera permission denied
            }

        }

    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.handleIntent()
    }
    private fun Intent.handleIntent() {
        when (action) {
            // When the BII is matched, Intent.Action_VIEW will be used
            Intent.ACTION_VIEW -> handleIntent(data)
            // Otherwise start the app as you would normally do.
            else -> Unit
        }
    }
    private fun handleIntent(data: Uri?) {
        // path is normally used to indicate which view should be displayed
        // i.e https://fit-actions.firebaseapp.com/start?exerciseType="Running" -> path = "start"
        var actionHandled = true

        val startExercise = intent?.extras?.getString("feature")
        // Add stopExercise variable here

        if (startExercise != null){
            Log.i("spoken text slice","opened")
        } // Add conditional for stopExercise

//        notifyActionSuccess(actionHandled)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) -> {
                setCameraPreview()
            }

            else -> {
                cameraPermissionRequest.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun captureImage(imageCapture: ImageCapture) {
        lifecycleScope.launch {
            while (true) {
                imageCapture.takePicture(
                    ContextCompat.getMainExecutor(this@MainActivity.applicationContext),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            Log.i("hello image", image.toString())
                        }

                        override fun onError(exception: ImageCaptureException) {
                            throw exception
                        }
                    })
                delay(1000)
            }
        }
    }

    private fun setCameraPreview() {
        setContent {
            VisionAiTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel = hiltViewModel<CameraViewModel>()
                    val state by viewModel.state.collectAsState()
                    CameraView(
                        labeler = labeler,
                        state = state,
                        langCode = "en"
                    ) {
                        viewModel.onEvent(it)
                    }
//                    CameraPreviewScreen(labeler)
                }
            }
        }
    }
}