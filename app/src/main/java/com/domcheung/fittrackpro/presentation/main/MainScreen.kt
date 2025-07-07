package com.domcheung.fittrackpro.presentation.main

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.domcheung.fittrackpro.navigation.Routes
import com.domcheung.fittrackpro.presentation.home.HomeScreen
import com.domcheung.fittrackpro.presentation.model.MainTab
import com.domcheung.fittrackpro.presentation.profile.ProfileScreen
import com.domcheung.fittrackpro.presentation.progress.ProgressScreen
import com.domcheung.fittrackpro.presentation.workout.WorkoutScreen
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun MainTabScreen(
    navController: NavHostController, // Receive the NavController
    onSignOut: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    Scaffold(
        bottomBar = {
            FitTrackBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    if (tab == MainTab.START) {
                        // TODO: Implement proper quick start logic later
                        // For now, it just switches to the WORKOUT tab
                        selectedTab = MainTab.WORKOUT
                    } else {
                        selectedTab = tab
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "tab_content"
            ) { tab ->
                when (tab) {
                    MainTab.HOME -> HomeScreen(
                        onNavigateToWorkout = { selectedTab = MainTab.WORKOUT },
                        onNavigateToProgress = { selectedTab = MainTab.PROGRESS },
                        onNavigateToWorkoutSession = { sessionId ->
                            // Use the NavController to navigate to the session screen
                            navController.navigate(Routes.workoutSession(sessionId))
                        }
                    )

                    MainTab.WORKOUT -> WorkoutScreen(
                        onNavigateToWorkoutSession = { sessionId ->
                            // Use the NavController to navigate to the session screen
                            navController.navigate(Routes.workoutSession(sessionId))
                        },
                        onNavigateToCreatePlan = {
                            // TODO: Navigate to create plan screen
                            // navController.navigate("create_plan")
                        }
                    )

                    MainTab.START -> {
                        // This case is now handled in onTabSelected,
                        // but as a fallback, show the Workout screen.
                        WorkoutScreen(
                            onNavigateToWorkoutSession = { sessionId ->
                                navController.navigate(Routes.workoutSession(sessionId))
                            },
                            onNavigateToCreatePlan = { /* TODO */ }
                        )
                    }

                    MainTab.PROGRESS -> ProgressScreen()

                    MainTab.PROFILE -> ProfileScreen(
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }
}