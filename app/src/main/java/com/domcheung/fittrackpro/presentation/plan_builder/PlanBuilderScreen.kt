package com.domcheung.fittrackpro.presentation.plan_builder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * The main screen for building and editing a workout plan.
 */
@OptIn(ExperimentalMaterial3Api::class) // Required for TopAppBar
@Composable
fun PlanBuilderScreen(
    viewModel: PlanBuilderViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // We use Scaffold as the root layout to easily place the TopAppBar and FloatingActionButton.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Plan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implement Save logic */ }) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Save Plan"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Use LargeFloatingActionButton for a bigger, circular button without text.
            LargeFloatingActionButton(
                onClick = { /* TODO: Implement Add Exercise logic */ },
                shape = CircleShape, // Enforce a perfect circle shape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Exercise",
                    // Increase the size of the icon itself to make it more prominent.
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        // This places the FAB in the bottom center, docked with the bottom bar area.
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->

        // This Column is the main content area of the screen.
        // We will add the plan name and exercise list here in the next steps.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding to avoid content being under the bars
        ) {
            // Placeholder for our next development step
            Text(text = "Plan content will go here...")
        }
    }
}