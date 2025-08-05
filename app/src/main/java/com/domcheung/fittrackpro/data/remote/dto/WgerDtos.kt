package com.domcheung.fittrackpro.data.remote.dto

import com.google.gson.annotations.SerializedName

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
 * DTO for a single exercise from the Wger API's `/api/v2/exercise/` endpoint.
 * This contains the essential information we need to create our local `Exercise` entity.
 */
data class ExerciseDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("category")
    val categoryId: Int,
    @SerializedName("equipment")
    val equipmentIds: List<Int>?,
    @SerializedName("language")
    val languageId: Int
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