package com.example.vision_ai.feature.voice_to_text.presentation.model

sealed interface VoiceToTextEvent {
    data object Close : VoiceToTextEvent
    data class PermissionResult(
        val isGranted:Boolean,
        val isPermissionDeclined:Boolean
    ): VoiceToTextEvent
    data class ToggleRecording(val langCode:String): VoiceToTextEvent
    data object Reset: VoiceToTextEvent
}