package com.domcheung.fittrackpro.presentation.plan_builder

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * The main screen for building and editing a workout plan.
 * This is a placeholder for now.
 */
@Composable
fun PlanBuilderScreen(
    viewModel: PlanBuilderViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // For now, it's just a placeholder text.
    // We will build the detailed UI from our wireframe in the next step.
    Text(text = "This is the Plan Builder Screen.")
}