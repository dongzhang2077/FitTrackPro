package com.domcheung.fittrackpro.data.remote

import com.domcheung.fittrackpro.data.remote.dto.EquipmentDto
import com.domcheung.fittrackpro.data.remote.dto.ExerciseCategoryDto
import com.domcheung.fittrackpro.data.remote.dto.ExerciseDto
import com.domcheung.fittrackpro.data.remote.dto.ExerciseImageResponseDto
import com.domcheung.fittrackpro.data.remote.dto.WgerApiResponse
import retrofit2.http.GET

/**
 * Defines the endpoints for the Wger API using Retrofit.
 */
interface WgerApiService {

    /**
     * Fetches a list of all exercises with full info including translations.
     * The exerciseinfo endpoint provides exercise names in the translations array.
     * The language filter=2 ensures we get English exercises.
     */
    @GET("exerciseinfo/?language=2&limit=1000")
    suspend fun getAllExercises(): WgerApiResponse<ExerciseDto>

    /**
     * Fetches the list of all available exercise categories (e.g., Chest, Back, Legs).
     */
    @GET("exercisecategory/?limit=100")
    suspend fun getAllExerciseCategories(): WgerApiResponse<ExerciseCategoryDto>

    /**
     * Fetches the list of all available equipment (e.g., Barbell, Dumbbell).
     */
    @GET("equipment/?limit=100")
    suspend fun getAllEquipment(): WgerApiResponse<EquipmentDto>

    /**
     * Fetches exercise images from the API.
     * Note: The exerciseinfo endpoint doesn't include images, so we fetch them separately.
     */
    @GET("exerciseimage/?limit=1000")
    suspend fun getAllExerciseImages(): WgerApiResponse<ExerciseImageResponseDto>
}