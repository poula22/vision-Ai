package com.example.vision_ai.camera.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vision_ai.camera.model.CameraState
import com.example.vision_ai.camera.model.DisplayState
import com.example.vision_ai.voice_to_text.presentation.model.VoiceToTextEvent
import com.example.vision_ai.voice_to_text.presentation.parser.VoiceToTextParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val parser: VoiceToTextParser
) : ViewModel() {
    private val _state = MutableStateFlow(CameraState())
    val state = _state.combine(parser.state) { state, voiceResult ->
        state.copy(
            spokenText = voiceResult.result,
            recordError = if (state.canRecord) {
                voiceResult.error
            } else {
                "Can't record without permission"
            },
            displayState = when {
                !state.canRecord || voiceResult.error != null -> DisplayState.ERROR
                voiceResult.result.isNotBlank() && voiceResult.isSpeaking -> {
                    DisplayState.DISPLAY_RESULT
                }
                voiceResult.isSpeaking -> DisplayState.SPEAKING
                else -> {
                    DisplayState.WAITING_TO_TALK
                }
            }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CameraState()
    )

    fun onEvent(event: VoiceToTextEvent) {
        when (event) {
            is VoiceToTextEvent.PermissionResult -> {
                _state.update {
                    it.copy(canRecord = event.isGranted)
                }
            }
            VoiceToTextEvent.Reset -> {
                parser.reset()
                _state.update { CameraState() }
            }
            is VoiceToTextEvent.ToggleRecording -> {
                toggleRecording(event.langCode)
            }
            else -> Unit
        }
    }

    private fun toggleRecording(langCode: String) {
        parser.cancel()
        parser.startListening(langCode)
    }
}