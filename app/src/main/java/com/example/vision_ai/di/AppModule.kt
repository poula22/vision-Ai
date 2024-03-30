package com.example.vision_ai.di

import android.app.Application
import com.example.vision_ai.feature.voice_to_text.presentation.parser.VoiceToTextParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object VoiceToTextModule {
    @Provides
    @ViewModelScoped
    fun provideVoiceToTextParser(app: Application): VoiceToTextParser {
        return VoiceToTextParser(app)
    }
}