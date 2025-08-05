package com.domcheung.fittrackpro.data.remote

import com.domcheung.fittrackpro.data.remote.dto.EquipmentDto
import com.domcheung.fittrackpro.data.remote.dto.ExerciseCategoryDto
import com.domcheung.fittrackpro.data.remote.dto.ExerciseDto
import com.domcheung.fittrackpro.data.remote.dto.WgerApiResponse
import retrofit2.http.GET

/**
 * Defines the endpoints for the Wger API using Retrofit.
 */
interface WgerApiService {

    /**
     * Fetches a list of all exercises.
     * The language is hardcoded to 2, which corresponds to English in the Wger API.
     * The limit is set to a high number to fetch all exercises at once.
     */
    @GET("exercise/?language=2&limit=1000")
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
}