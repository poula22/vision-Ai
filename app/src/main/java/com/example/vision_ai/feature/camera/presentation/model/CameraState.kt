package com.example.vision_ai.feature.camera.presentation.model

data class CameraState(
    val spokenText: String = "",
    val canRecord: Boolean = false,
    val recordError: String? =null,
    val displayState: DisplayState = DisplayState.WAITING_TO_TALK
)
enum class DisplayState{
    WAITING_TO_TALK,
    SPEAKING,
    DISPLAY_RESULT,
    ERROR
}