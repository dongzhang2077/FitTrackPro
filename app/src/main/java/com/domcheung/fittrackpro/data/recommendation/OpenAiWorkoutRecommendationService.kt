package com.domcheung.fittrackpro.data.recommendation

import com.domcheung.fittrackpro.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAiWorkoutRecommendationService @Inject constructor(
    private val okHttpClient: OkHttpClient
) : AiWorkoutRecommendationService {

    private val gson = Gson()

    override suspend fun recommendPlan(context: AiRecommendationContext): AiPlanRecommendation? {
        if (!isConfigured()) {
            return null
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                val requestJson = buildRequestJson(context)
                val request = Request.Builder()
                    .url(BuildConfig.AI_RECOMMENDER_ENDPOINT)
                    .addHeader("Authorization", "Bearer ${BuildConfig.AI_RECOMMENDER_API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .post(requestJson.toRequestBody("application/json".toMediaType()))
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@use null
                    }

                    val body = response.body?.string().orEmpty()
                    if (body.isBlank()) {
                        return@use null
                    }

                    parseRecommendation(body, context)
                }
            }.getOrNull()
        }
    }

    private fun isConfigured(): Boolean {
        return BuildConfig.AI_RECOMMENDER_API_KEY.isNotBlank() &&
            BuildConfig.AI_RECOMMENDER_ENDPOINT.isNotBlank() &&
            BuildConfig.AI_RECOMMENDER_MODEL.isNotBlank()
    }

    private fun buildRequestJson(context: AiRecommendationContext): String {
        val planList = context.plans.joinToString(separator = "\n") { plan ->
            "- id=${plan.id}, name=${plan.name}, duration=${plan.estimatedDuration}, muscles=${plan.targetMuscleGroups.joinToString(",")}, tags=${plan.tags.joinToString(",")}" 
        }

        val recentIds = if (context.recentCompletedPlanIds.isEmpty()) {
            "none"
        } else {
            context.recentCompletedPlanIds.joinToString(",")
        }

        val systemPrompt = "You are a fitness coach assistant. Pick one workout plan id and explain briefly."
        val userPrompt = """
            Recommend one plan for today.
            Day of week (1=Mon..7=Sun): ${context.dayOfWeek}
            Primary goal: ${context.primaryGoal}
            Preferred workouts per week: ${context.preferredWorkoutFrequency}
            Experience level: ${context.experienceLevel}
            Recent completed plan ids (newest first): $recentIds
            Candidate plans:
            $planList

            Return strict JSON only with fields:
            {"planId":"<id>","reason":"<short sentence under 20 words>"}
        """.trimIndent()

        val payload = mapOf(
            "model" to BuildConfig.AI_RECOMMENDER_MODEL,
            "temperature" to 0.2,
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to userPrompt)
            )
        )

        return gson.toJson(payload)
    }

    private fun parseRecommendation(responseBody: String, context: AiRecommendationContext): AiPlanRecommendation? {
        val root = gson.fromJson(responseBody, JsonObject::class.java)
        val content = root
            ?.getAsJsonArray("choices")
            ?.firstOrNull()
            ?.asJsonObject
            ?.getAsJsonObject("message")
            ?.get("content")
            ?.asString
            ?.trim()
            ?: return null

        val parsed = runCatching {
            gson.fromJson(content, JsonObject::class.java)
        }.getOrNull() ?: return null

        val planId = parsed.get("planId")?.asString?.trim().orEmpty()
        val reason = parsed.get("reason")?.asString?.trim().orEmpty()

        if (planId.isBlank() || reason.isBlank()) {
            return null
        }

        val validPlanIds = context.plans.map { it.id }.toSet()
        if (planId !in validPlanIds) {
            return null
        }

        return AiPlanRecommendation(planId = planId, reason = reason)
    }
}
