package com.domcheung.fittrackpro.presentation.workout

import com.domcheung.fittrackpro.data.model.WorkoutPlan

data class TemplateFilterOption(
    val tag: String,
    val label: String
)

object WorkoutTemplateFilters {
    val options: List<TemplateFilterOption> = listOf(
        TemplateFilterOption(tag = "goal_general_fitness", label = "General"),
        TemplateFilterOption(tag = "goal_muscle_gain", label = "Muscle Gain"),
        TemplateFilterOption(tag = "goal_fat_loss", label = "Fat Loss"),
        TemplateFilterOption(tag = "goal_strength", label = "Strength"),
        TemplateFilterOption(tag = "frequency_3", label = "3x Week"),
        TemplateFilterOption(tag = "frequency_5", label = "5x Week"),
        TemplateFilterOption(tag = "frequency_7", label = "Daily"),
        TemplateFilterOption(tag = "intensity_light", label = "Light"),
        TemplateFilterOption(tag = "intensity_medium", label = "Medium"),
        TemplateFilterOption(tag = "intensity_heavy", label = "Heavy"),
        TemplateFilterOption(tag = "full_body", label = "Full Body"),
        TemplateFilterOption(tag = "push", label = "Push"),
        TemplateFilterOption(tag = "pull", label = "Pull"),
        TemplateFilterOption(tag = "legs", label = "Legs")
    )
}

fun WorkoutPlan.matchesSelectedTemplateTags(selectedTags: Set<String>): Boolean {
    if (selectedTags.isEmpty()) {
        return true
    }

    if (tags.isEmpty()) {
        return false
    }

    val normalizedPlanTags = tags.map { tag -> tag.lowercase() }.toSet()
    return selectedTags.any { selectedTag ->
        normalizedPlanTags.contains(selectedTag.lowercase())
    }
}
