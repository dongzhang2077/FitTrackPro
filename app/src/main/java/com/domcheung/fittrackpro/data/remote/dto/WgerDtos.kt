package com.domcheung.fittrackpro.data.remote.dto

import com.google.gson.annotations.SerializedName
/**
 * DTO for exercise videos
 */
data class ExerciseVideoDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("uuid")
    val uuid: String? = null,
    @SerializedName("exercise_base")
    val exerciseBase: Int? = null,
    @SerializedName("exercise_base_id")
    val exerciseBaseId: Int? = null,
    @SerializedName("exercise")
    val exerciseId: Int? = null,
    @SerializedName("video")
    val videoUrl: String,
    @SerializedName("is_main")
    val isMain: Boolean,
    @SerializedName("size")
    val size: Int? = null,
    @SerializedName("duration")
    val duration: String? = null,
    @SerializedName("width")
    val width: Int? = null,
    @SerializedName("height")
    val height: Int? = null,
    @SerializedName("codec")
    val codec: String? = null,
    @SerializedName("codec_long")
    val codecLong: String? = null,
    @SerializedName("license")
    val license: Int? = null,
    @SerializedName("license_author")
    val licenseAuthor: String? = null
) {
    // Helper function to get the actual exercise ID from whichever field has it
    fun resolveExerciseId(): Int? = exerciseBaseId ?: exerciseBase ?: exerciseId
}

/**
 * DTO for exercise base data from /exercise/ endpoint
 * This helps map between base IDs (used by images) and info IDs (used by exerciseinfo)
 */
data class ExerciseBaseDto(
    @SerializedName("id")
    val id: Int,  // This is the exerciseinfo ID
    @SerializedName("exercise_base")
    val exerciseBase: Int,  // This is the base ID used by images
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("description")
    val description: String? = null
)

/**
 * This file contains the Data Transfer Objects (DTOs) that directly match the JSON structure
 * returned by the Wger API. Gson will use these classes to parse the network response.
 */

/**
 * A generic class representing the paginated response structure from the Wger API.
 * @param T The type of the data items in the 'results' list.
 */
data class WgerApiResponse<T>(
    @SerializedName("count")
    val count: Int,
    @SerializedName("next")
    val next: String?,
    @SerializedName("previous")
    val previous: String?,
    @SerializedName("results")
    val results: List<T>
)

/**
 * DTO for a single exercise from the Wger API's `/api/v2/exerciseinfo/` endpoint.
 * This endpoint provides complete exercise information including translations with names.
 */
data class ExerciseDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("uuid")
    val uuid: String? = null,
    @SerializedName("created")
    val created: String? = null,
    @SerializedName("last_update")
    val lastUpdate: String? = null,
    @SerializedName("category")
    val category: ExerciseCategoryDto?,  // Nested object
    @SerializedName("muscles")
    val muscles: List<MuscleDto>? = null,  // Array of muscle objects
    @SerializedName("muscles_secondary")
    val musclesSecondary: List<MuscleDto>? = null,  // Array of muscle objects
    @SerializedName("equipment")
    val equipment: List<EquipmentDto>? = null,  // Array of objects
    @SerializedName("translations")
    val translations: List<TranslationDto>? = null,  // Contains the exercise names!
    @SerializedName("images")
    val images: List<ExerciseImageDto>? = null,  // Exercise images
    @SerializedName("videos")
    val videos: List<ExerciseVideoDto>? = null,
    @SerializedName("license_author")
    val licenseAuthor: String? = null
)

/**
 * DTO for exercise translation (contains the actual exercise name and description)
 */
data class TranslationDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("uuid")
    val uuid: String? = null,
    @SerializedName("name")
    val name: String,  // THIS IS THE EXERCISE NAME!
    @SerializedName("description")
    val description: String?,
    @SerializedName("language")
    val languageId: Int,  // 2 = English
    @SerializedName("aliases")
    val aliases: List<Any>? = null  // Can be strings or objects, so use Any
)

/**
 * DTO for an exercise category from the Wger API's `/api/v2/exercisecategory/` endpoint.
 */
data class ExerciseCategoryDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)

/**
 * DTO for a piece of equipment from the Wger API's `/api/v2/equipment/` endpoint.
 */
data class EquipmentDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)

/**
 * DTO for muscle information
 */
data class MuscleDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("name_en")
    val nameEn: String? = null,
    @SerializedName("is_front")
    val isFront: Boolean? = null
)

/**
 * DTO for exercise images
 */
data class ExerciseImageDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("image")
    val imageUrl: String? = null,
    @SerializedName("is_main")
    val isMain: Boolean? = null
)

/**
 * DTO for exercise image response from /exerciseimage/ endpoint
 */
data class ExerciseImageResponseDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("uuid")
    val uuid: String? = null,
    @SerializedName("exercise_base")
    val exerciseBase: Int? = null,  // Try without the Id suffix
    @SerializedName("exercise_base_id")
    val exerciseBaseId: Int? = null,  // Also try with the Id suffix
    @SerializedName("exercise")
    val exerciseId: Int? = null,  // Some APIs use just "exercise"
    @SerializedName("image")
    val imageUrl: String,
    @SerializedName("is_main")
    val isMain: Boolean,
    @SerializedName("style")
    val style: String? = null,
    @SerializedName("license")
    val license: Int? = null,
    @SerializedName("license_author")
    val licenseAuthor: String? = null
) {
    // Helper function to get the actual exercise ID from whichever field has it
    fun resolveExerciseId(): Int? = exerciseBaseId ?: exerciseBase ?: exerciseId
}