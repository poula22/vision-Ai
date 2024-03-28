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
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.lifecycle.lifecycleScope
import com.example.vision_ai.camera.CameraPreviewScreen
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


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
                            Log.i("hello image",image.toString())
                        }
                        override fun onError(exception: ImageCaptureException) {
                            throw exception
                        }
                    })
                delay(1000)
            }
        }
    }

    @OptIn(ExperimentalGetImage::class) private fun analysisImage(imageAnalysis: ImageAnalysis) {
        imageAnalysis.setAnalyzer(Executors.newFixedThreadPool(4), ImageAnalysis.Analyzer { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            // insert your code here.
            val map = imageProxy.toBitmap()
            labeler.process(map,0)
                .addOnSuccessListener { labels ->
                    // Task completed successfully
                    // ...
                    Log.i("hellllo",labels.get(0).text)
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                }


            // after done, release the ImageProxy object
            imageProxy.close()
        })
    }

    private fun setCameraPreview() {
        setContent {
            VisionAiTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraPreviewScreen(::captureImage,::analysisImage)
                }
            }
        }
    }
}