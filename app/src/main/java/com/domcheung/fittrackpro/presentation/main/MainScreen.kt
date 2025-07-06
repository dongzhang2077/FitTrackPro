package com.domcheung.fittrackpro.presentation.main

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import com.domcheung.fittrackpro.presentation.home.HomeScreen
import com.domcheung.fittrackpro.presentation.workout.WorkoutScreen
import com.domcheung.fittrackpro.presentation.progress.ProgressScreen
import com.domcheung.fittrackpro.presentation.profile.ProfileScreen
import com.domcheung.fittrackpro.presentation.model.MainTab
import com.domcheung.fittrackpro.data.repository.AuthRepository

@Composable
fun MainTabScreen(
    onSignOut: () -> Unit = {},
    authRepository: AuthRepository = hiltViewModel<MainTabViewModel>().authRepository
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            FitTrackBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    if (tab == MainTab.START) {
                        // START button now navigates to Home and triggers quick start
                        selectedTab = MainTab.HOME
                        // The HomeScreen will handle the quick start logic
                        Toast.makeText(context, "Quick Start Workout!", Toast.LENGTH_SHORT).show()
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
            // Tab content with fade animation
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
                        onNavigateToWorkout = {
                            // Navigate to Workout tab
                            selectedTab = MainTab.WORKOUT
                        },
                        onNavigateToProgress = {
                            // Navigate to Progress tab
                            selectedTab = MainTab.PROGRESS
                        },
                        onNavigateToWorkoutSession = { sessionId ->
                            // TODO: Navigate to workout execution screen
                            // For now, show a toast and navigate to workout tab
                            Toast.makeText(
                                context,
                                "Starting workout session: $sessionId",
                                Toast.LENGTH_SHORT
                            ).show()
                            selectedTab = MainTab.WORKOUT
                        }
                    )

                    MainTab.WORKOUT -> WorkoutScreen(
                        onNavigateToWorkoutSession = { sessionId ->
                            // TODO: Navigate to workout execution screen
                            // For now, show a toast
                            Toast.makeText(
                                context,
                                "Starting workout session: $sessionId",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Could also stay on workout tab or navigate to a specific workout execution screen
                        },
                        onNavigateToCreatePlan = {
                            // TODO: Navigate to create plan screen
                            // For now, show a toast
                            Toast.makeText(
                                context,
                                "Create Plan - Coming Soon!",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Could navigate to a dedicated create plan screen
                        }
                    )

                    MainTab.START -> {
                        // Fallback, shouldn't be reached
                        HomeScreen(
                            onNavigateToWorkout = { selectedTab = MainTab.WORKOUT },
                            onNavigateToProgress = { selectedTab = MainTab.PROGRESS },
                            onNavigateToWorkoutSession = { sessionId ->
                                Toast.makeText(
                                    context,
                                    "Starting workout session: $sessionId",
                                    Toast.LENGTH_SHORT
                                ).show()
                                selectedTab = MainTab.WORKOUT
                            }
                        )
                    }

                    MainTab.PROGRESS -> ProgressScreen()

                    MainTab.PROFILE -> ProfileScreen(
                        onSignOut = {
                            println("DEBUG: MainTabScreen - onSignOut called")
                            // First call AuthRepository signOut to clear data
                            authRepository.signOut()
                            // Then call navigation callback
                            onSignOut()
                        }
                    )
                }
            }
        }
    }
}