package com.domcheung.fittrackpro.di

import com.domcheung.fittrackpro.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing use case dependencies
 * Provides all workout-related use cases for dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // ========== Exercise Use Cases ==========

    @Provides
    @Singleton
    fun provideSyncExercisesFromApiUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): SyncExercisesFromApiUseCase {
        return SyncExercisesFromApiUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideGetExerciseByIdUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): GetExerciseByIdUseCase {
        return GetExerciseByIdUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideSearchExercisesUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): SearchExercisesUseCase {
        return SearchExercisesUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideGetExercisesByCategoryUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): GetExercisesByCategoryUseCase {
        return GetExercisesByCategoryUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideCreateCustomExerciseUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): CreateCustomExerciseUseCase {
        return CreateCustomExerciseUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideGetAllExercisesUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): GetAllExercisesUseCase {
        return GetAllExercisesUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideGetExercisesByIdsUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): GetExercisesByIdsUseCase {
        return GetExercisesByIdsUseCase(workoutRepository)
    }

    // ========== Workout Plan Use Cases ==========

    @Provides
    @Singleton
    fun provideCreateWorkoutPlanUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): CreateWorkoutPlanUseCase {
        return CreateWorkoutPlanUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserWorkoutPlansUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): GetUserWorkoutPlansUseCase {
        return GetUserWorkoutPlansUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideCopyWorkoutPlanUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): CopyWorkoutPlanUseCase {
        return CopyWorkoutPlanUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteWorkoutPlanUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): DeleteWorkoutPlanUseCase {
        return DeleteWorkoutPlanUseCase(workoutRepository)
    }

    // ========== Workout Session Use Cases ==========

    @Provides
    @Singleton
    fun provideStartWorkoutSessionUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): StartWorkoutSessionUseCase {
        return StartWorkoutSessionUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideGetActiveWorkoutSessionUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): GetActiveWorkoutSessionUseCase {
        return GetActiveWorkoutSessionUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideCompleteWorkoutSessionUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): CompleteWorkoutSessionUseCase {
        return CompleteWorkoutSessionUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun providePauseWorkoutSessionUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): PauseWorkoutSessionUseCase {
        return PauseWorkoutSessionUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideResumeWorkoutSessionUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): ResumeWorkoutSessionUseCase {
        return ResumeWorkoutSessionUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideGetWorkoutSessionUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): GetWorkoutSessionUseCase {
        return GetWorkoutSessionUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideAbandonWorkoutSessionUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): AbandonWorkoutSessionUseCase {
        return AbandonWorkoutSessionUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideGetWorkoutSessionByIdFlowUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): GetWorkoutSessionByIdFlowUseCase {
        return GetWorkoutSessionByIdFlowUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateWorkoutSessionUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): UpdateWorkoutSessionUseCase {
        return UpdateWorkoutSessionUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideCheckAndCreatePersonalRecordUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): CheckAndCreatePersonalRecordUseCase {
        return CheckAndCreatePersonalRecordUseCase(workoutRepository)
    }

    // ========== Statistics Use Cases ==========

    @Provides
    @Singleton
    fun provideGetWorkoutStatisticsUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): GetWorkoutStatisticsUseCase {
        return GetWorkoutStatisticsUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideGetPersonalRecordsUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): GetPersonalRecordsUseCase {
        return GetPersonalRecordsUseCase(workoutRepository)
    }

    // ========== Sync Use Cases ==========

    @Provides
    @Singleton
    fun provideSyncExercisesUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): SyncExercisesUseCase {
        return SyncExercisesUseCase(workoutRepository)
    }

    // ========== Summary Use Cases ==========

    @Provides
    @Singleton
    fun provideGetMonthlyWorkoutSummaryUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): GetMonthlyWorkoutSummaryUseCase {
        return GetMonthlyWorkoutSummaryUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideGetWeeklyWorkoutSummaryUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): GetWeeklyWorkoutSummaryUseCase {
        return GetWeeklyWorkoutSummaryUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideGetYearlyWorkoutSummaryUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): GetYearlyWorkoutSummaryUseCase {
        return GetYearlyWorkoutSummaryUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideGetTodaysRecommendedPlanUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): GetTodaysRecommendedPlanUseCase {
        return GetTodaysRecommendedPlanUseCase(workoutRepository)
    }

    @Provides
    @Singleton
    fun provideSyncDataUseCase(
        workoutRepository: com.domcheung.fittrackpro.data.repository.WorkoutRepository
    ): SyncDataUseCase {
        return SyncDataUseCase(workoutRepository)
    }
}