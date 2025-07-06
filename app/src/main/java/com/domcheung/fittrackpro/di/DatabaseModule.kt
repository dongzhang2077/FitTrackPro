package com.domcheung.fittrackpro.di

import android.content.Context
import androidx.room.Room
import com.domcheung.fittrackpro.data.local.AppDatabase
import com.domcheung.fittrackpro.data.local.dao.*
import com.domcheung.fittrackpro.data.repository.WorkoutRepository
import com.domcheung.fittrackpro.data.repository.WorkoutRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database and repository dependencies
 * Uses KSP for annotation processing and provides all workout-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // ========== Database Providers ==========

    /**
     * Provides the Room database instance
     * Singleton ensures only one database instance throughout the app lifecycle
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Only for development - remove in production
            .build()
    }

    // ========== DAO Providers ==========

    /**
     * Provides ExerciseDao from the database
     */
    @Provides
    @Singleton
    fun provideExerciseDao(database: AppDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    /**
     * Provides WorkoutPlanDao from the database
     */
    @Provides
    @Singleton
    fun provideWorkoutPlanDao(database: AppDatabase): WorkoutPlanDao {
        return database.workoutPlanDao()
    }

    /**
     * Provides WorkoutSessionDao from the database
     */
    @Provides
    @Singleton
    fun provideWorkoutSessionDao(database: AppDatabase): WorkoutSessionDao {
        return database.workoutSessionDao()
    }

    /**
     * Provides PersonalRecordDao from the database
     */
    @Provides
    @Singleton
    fun providePersonalRecordDao(database: AppDatabase): PersonalRecordDao {
        return database.personalRecordDao()
    }

    // ========== Firebase Providers ==========
    // Note: FirebaseFirestore is provided by AppModule to avoid duplicate bindings

    // ========== Repository Providers ==========

    /**
     * Provides WorkoutRepository implementation
     * Binds the implementation to the interface for dependency injection
     */
    @Provides
    @Singleton
    fun provideWorkoutRepository(
        exerciseDao: ExerciseDao,
        workoutPlanDao: WorkoutPlanDao,
        workoutSessionDao: WorkoutSessionDao,
        personalRecordDao: PersonalRecordDao,
        firestore: FirebaseFirestore
    ): WorkoutRepository {
        return WorkoutRepositoryImpl(
            exerciseDao = exerciseDao,
            workoutPlanDao = workoutPlanDao,
            workoutSessionDao = workoutSessionDao,
            personalRecordDao = personalRecordDao,
            firestore = firestore
        )
    }
}