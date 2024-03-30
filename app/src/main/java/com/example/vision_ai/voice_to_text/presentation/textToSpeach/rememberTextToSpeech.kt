package com.example.vision_ai.voice_to_text.presentation.textToSpeach

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext


@Composable
fun rememberTextToSpeech() : TextToSpeech {
    val context = LocalContext.current
    val textToSpeech = remember {
        TextToSpeech(context,null)
    }
    DisposableEffect(key1 = textToSpeech ){
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }
    return textToSpeech
}