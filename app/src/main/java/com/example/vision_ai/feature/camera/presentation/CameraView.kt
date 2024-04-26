package com.example.vision_ai.feature.camera.presentation

import android.Manifest
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.vision_ai.feature.camera.presentation.model.CameraState
import com.example.vision_ai.feature.voice_to_text.presentation.model.VoiceToTextEvent
import com.example.vision_ai.feature.text_to_speech.rememberTextToSpeech
import com.google.mlkit.vision.label.ImageLabeler
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun CameraView(
    labeler: ImageLabeler,
    state: CameraState,
    langCode: String,
    onEvent: (VoiceToTextEvent) -> Unit,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val cameraController = remember {
        LifecycleCameraController(context).also {
            it.bindToLifecycle(lifecycle)

        }
    }
    val previewView = remember {
        PreviewView(context).also {
            it.controller = cameraController
        }
    }

    var text by remember {
        mutableStateOf("")
    }

    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            onEvent(
                VoiceToTextEvent.PermissionResult(
                    isGranted = isGranted,
                    isPermissionDeclined = !isGranted && !(context as ComponentActivity)
                        .shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
                )
            )
        }
    )
    val textToSpeech = rememberTextToSpeech()
    textToSpeech.language = Locale.ENGLISH


//    LaunchedEffect(recordAudioLauncher) {
//        recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
//        onEvent(VoiceToTextEvent.ToggleRecording("en"))
//    }


    LaunchedEffect(key1 = cameraController.cameraInfo) {

        delay(100L)
        while (true) {
            cameraController.takePicture(ContextCompat.getMainExecutor(context),
                object : OnImageCapturedCallback() {
                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        // insert your code here.
                        val map = imageProxy.toBitmap()
                        labeler.process(map, rotationDegrees)
                            .addOnSuccessListener { labels ->
                                var maxConfidence = 0.0f
                                var currLabel = ""
                                for (label in labels) {
                                    if (label.confidence>maxConfidence) {
                                        maxConfidence = label.confidence
                                        currLabel = label.text
                                    }
                                }
                                Log.i("label.confidence",rotationDegrees.toString())
                                text = currLabel
                                textToSpeech.speak(
                                    text,
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    null
                                )
                            }
                            .addOnFailureListener { e ->
                                // Task failed with an exception
                                // ...
                            }
                        // after done, release the ImageProxy object
                        imageProxy.close()
                    }
                })
            delay(3500L)
        }

    }

//    LaunchedEffect(key1 = true) {
//        cameraController
//            .setImageAnalysisAnalyzer(
//            Executors.newSingleThreadExecutor()
//        ) { imageProxy->
//            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
//            // insert your code here.
//            val map = imageProxy.toBitmap()
//            labeler.process(map,rotationDegrees)
//                .addOnSuccessListener { labels ->
//                    for (label in labels){
//                        text =label.text
//                    }
//                }
//                .addOnFailureListener { e ->
//                    // Task failed with an exception
//                    // ...
//                }
//
//
//            // after done, release the ImageProxy object
//            imageProxy.close()
//        }
//    }

    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier
            .fillMaxSize()
        )
        Text(text = text)
//        Text(text = state.recordError ?: "")
    }
}