package com.example.vision_ai.voice_to_text.presentation.parser

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.vision_ai.voice_to_text.presentation.model.VoiceToTextState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VoiceToTextParser(
    private val app: Application
) : RecognitionListener {
    private val recognizer = SpeechRecognizer.createSpeechRecognizer(app)
    private val _state = MutableStateFlow(VoiceToTextState())
    val state: StateFlow<VoiceToTextState>
        get() = _state.asStateFlow()
    fun startListening(langCode:String) {
        _state.update { VoiceToTextState() }
        if (!SpeechRecognizer.isRecognitionAvailable(app)) {
            _state.update {
                it.copy(
                    error = "error"
                )
            }
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000);
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra("android.speech.extra.DICTATION_MODE", true);
        }
        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)
        _state.update {
            it.copy(isSpeaking = true)
        }
    }
    fun stopListening() {
        _state.update { VoiceToTextState() }
        recognizer.stopListening()
    }
    fun reset() { _state.value = VoiceToTextState() }
    fun cancel() { recognizer.cancel() }
    override fun onReadyForSpeech(params: Bundle?) { _state.update { it.copy(error = null) } }
    override fun onEndOfSpeech() {
        _state.update {
            it.copy(isSpeaking = false)
        }
    }
    override fun onError(error: Int) {
        if (error == SpeechRecognizer.ERROR_CLIENT){
            return
        }
        _state.update {
            it.copy(error = "Error: $error")
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val list = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        Log.i("spoken text slice",list?.size.toString())
        list
            ?.getOrNull(list.size-1)
            ?.let { value ->
                Log.i("spoken text slice",value)
                _state.update {
                    it.copy(
                        result = value
                    )
                }
            }
    }
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onResults(results: Bundle?) = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}