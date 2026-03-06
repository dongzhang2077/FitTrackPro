package com.domcheung.fittrackpro.workout

import com.domcheung.fittrackpro.data.model.WorkoutPlan
import com.domcheung.fittrackpro.presentation.workout.WorkoutTemplateFilters
import com.domcheung.fittrackpro.presentation.workout.matchesSelectedTemplateTags
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutTemplateFiltersTest {

    @Test
    fun matchesSelectedTemplateTagsReturnsTrueWhenSelectionIsEmpty() {
        val plan = samplePlan(tags = emptyList())

        assertTrue(plan.matchesSelectedTemplateTags(emptySet()))
    }

    @Test
    fun matchesSelectedTemplateTagsReturnsTrueWhenAnyTagMatches() {
        val plan = samplePlan(tags = listOf("goal_muscle_gain", "frequency_4"))

        assertTrue(plan.matchesSelectedTemplateTags(setOf("goal_muscle_gain")))
        assertTrue(plan.matchesSelectedTemplateTags(setOf("intensity_light", "frequency_4")))
    }

    @Test
    fun matchesSelectedTemplateTagsReturnsFalseWhenNoTagsMatch() {
        val plan = samplePlan(tags = listOf("goal_strength", "intensity_heavy"))

        assertFalse(plan.matchesSelectedTemplateTags(setOf("goal_fat_loss")))
    }

    @Test
    fun optionsContainCoreV1TemplateTags() {
        val tags = WorkoutTemplateFilters.options.map { option -> option.tag }.toSet()

        assertTrue(tags.contains("goal_general_fitness"))
        assertTrue(tags.contains("goal_muscle_gain"))
        assertTrue(tags.contains("goal_fat_loss"))
        assertTrue(tags.contains("goal_strength"))
        assertTrue(tags.contains("frequency_3"))
        assertTrue(tags.contains("frequency_5"))
        assertTrue(tags.contains("frequency_7"))
        assertTrue(tags.contains("intensity_light"))
        assertTrue(tags.contains("intensity_medium"))
        assertTrue(tags.contains("intensity_heavy"))
    }

    private fun samplePlan(tags: List<String>): WorkoutPlan {
        return WorkoutPlan(
            id = "plan-1",
            name = "Plan",
            tags = tags
        )
    }
}
