package com.example.vision_ai.camera.adapter

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class CameraAdapter {
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    private val imageCapture = ImageCapture.Builder().build()
    fun captureImage(getImage :(String)->Unit, context:Context) {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    // insert your code here.
                    val map = imageProxy.toBitmap()
                    labeler.process(map,rotationDegrees)
                        .addOnSuccessListener { labels ->
                            for (label in labels){
                                getImage(label.text)
                            }
                        }
                        .addOnFailureListener { e ->
                            // Task failed with an exception
                            // ...
                        }

                    // after done, release the ImageProxy object
                    imageProxy.close()
                }
                override fun onError(exception: ImageCaptureException) {
                    throw exception
                }
            }
        )

    }
}