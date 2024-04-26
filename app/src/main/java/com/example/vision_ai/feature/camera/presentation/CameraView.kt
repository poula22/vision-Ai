package com.example.vision_ai.feature.camera.presentation

import android.Manifest
import android.graphics.Bitmap
import android.graphics.RectF
import android.speech.tts.TextToSpeech
import android.util.DisplayMetrics
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.vision_ai.feature.camera.presentation.model.CameraState
import com.example.vision_ai.feature.voice_to_text.presentation.model.VoiceToTextEvent
import com.example.vision_ai.feature.text_to_speech.rememberTextToSpeech
//import com.google.mlkit.vision.label.ImageLabeler
import kotlinx.coroutines.delay
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun CameraView(
//    labeler: ImageLabeler,
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

    val objectDetector = remember {
        ObjectDetector.createFromFileAndOptions(
            context,
            "1.tflite",
            ObjectDetector.ObjectDetectorOptions.builder()
                .setScoreThreshold(0.7f)
                .setMaxResults(10)
                .build()
        )
    }

    var boundingBoxWidth by remember {
        mutableFloatStateOf(0f)
    }

    var boundingBoxHeight by remember {
        mutableFloatStateOf(0f)
    }


    var score by remember {
        mutableFloatStateOf(0f)
    }



    val textToSpeech = rememberTextToSpeech()
    textToSpeech.language = Locale.ENGLISH


//    LaunchedEffect(recordAudioLauncher) {
//        recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
//        onEvent(VoiceToTextEvent.ToggleRecording("en"))
//    }


//    LaunchedEffect(key1 = cameraController.cameraInfo) {

//        delay(100L)
//        while (true) {
//            cameraController.takePicture(ContextCompat.getMainExecutor(context),
//                object : OnImageCapturedCallback() {
//                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
//                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
//                        // insert your code here.
//                        val imageProcessor = ImageProcessor.Builder()
//                            .add(Rot90Op(-rotationDegrees / 90))
//                            .build()
//                        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(imageProxy.toBitmap()))
//                        val results = objectDetector?.detect(tensorImage)
//                        results?.size?.let {size ->
//                            if (size>0) {
//                                val  categories = results[0]?.categories
//                                if (categories?.isNotEmpty() == true) {
//                                    val category = categories.get(0)
//                                    category?.let {
//                                        text = it.label
//                                    }
//                                }
//                            }
//                        }
//
//                        imageProxy.close()
//                    }
//                })
//            delay(200L)


//    }

    //                        val map = imageProxy.toBitmap()
//                        labeler.process(map, rotationDegrees)
//                            .addOnSuccessListener { labels ->
//                                var maxConfidence = 0.0f
//                                var currLabel = ""
//                                for (label in labels) {
//                                    if (label.confidence>maxConfidence) {
//                                        maxConfidence = label.confidence
//                                        currLabel = label.text
//                                    }
//                                }
//                                Log.i("label.confidence",rotationDegrees.toString())
//                                text = currLabel
//                                textToSpeech.speak(
//                                    text,
//                                    TextToSpeech.QUEUE_FLUSH,
//                                    null,
//                                    null
//                                )
//                            }
//                            .addOnFailureListener { e ->
//                                // Task failed with an exception
//                                // ...
//                            }
    // after done, release the ImageProxy object


    LaunchedEffect(key1 = true) {
        cameraController
            .setImageAnalysisAnalyzer(
                Executors.newSingleThreadExecutor()
            ) { imageProxy ->
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                // insert your code here.
                val map = imageProxy.toBitmap()
                val imageProcessor = ImageProcessor.Builder()
                            .add(Rot90Op(-rotationDegrees / 90))
                            .build()
                val tensorImage = imageProcessor.process(
                    TensorImage.fromBitmap(
                        imageProxy.toBitmap()
                    )
                )
                val results = objectDetector?.detect(tensorImage)
                results?.size?.let { size ->
                    if (size > 0) {
                        val categories = results[0]?.categories
                        val boundingBox = results[0]?.boundingBox

                        boundingBoxWidth = boundingBox?.width() ?: 0f
                        boundingBoxHeight = boundingBox?.height() ?: 0f
                        if (categories?.isNotEmpty() == true) {
                            val category = categories[0]
                            score = category.score
                            category?.let {
                                text = it.label
                                if (!textToSpeech.isSpeaking){
                                    textToSpeech.speak(
                                        text,
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        null
                                    )
                                }
                            }
                        }
                    }
                }


                // after done, release the ImageProxy object
                imageProxy.close()
            }
    }

    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {

        AndroidView(
            { previewView }, modifier = Modifier
                .fillMaxSize()
//                .align(Alignment.Center)
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RectangleShape)
                .padding(horizontal = 4.dp)
                .width(boundingBoxWidth.dp)
                .height(boundingBoxWidth.dp)
                .border(2.dp, Color.Yellow)
                .padding(vertical = 8.dp)
        ) {
            Text(
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .padding(4.dp)
                    .background(Color.Black),
                text = "$text $score",
                color = Color.White,
                fontSize = 16.sp
            )

        }

        Text(text = text)
//        Text(text = state.recordError ?: "")
    }
}