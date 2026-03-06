package com.domcheung.fittrackpro.di

import com.domcheung.fittrackpro.data.recommendation.AiWorkoutRecommendationService
import com.domcheung.fittrackpro.data.recommendation.OpenAiWorkoutRecommendationService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RecommendationModule {

    @Binds
    @Singleton
    abstract fun bindAiWorkoutRecommendationService(
        impl: OpenAiWorkoutRecommendationService
    ): AiWorkoutRecommendationService
}
