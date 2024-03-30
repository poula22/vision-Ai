package com.example.vision_ai.feature.voice_to_text.presentation.model

data class VoiceToTextState(
    val result: String ="",
    val error: String? = null,
    val isSpeaking: Boolean = false
)
