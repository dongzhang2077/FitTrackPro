package com.domcheung.fittrackpro.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.domcheung.fittrackpro.presentation.login.LoginScreen
import com.domcheung.fittrackpro.presentation.register.RegisterScreen
import com.domcheung.fittrackpro.presentation.main.MainTabScreen
import com.domcheung.fittrackpro.presentation.splash.SplashScreen
import com.domcheung.fittrackpro.presentation.workout_session.WorkoutSessionScreen

// Navigation routes
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"

    // New route for the workout session screen
    // It includes a placeholder for the sessionId argument
    const val WORKOUT_SESSION_ROUTE = "workout_session"
    const val WORKOUT_SESSION_ARG_ID = "sessionId"
    const val WORKOUT_SESSION = "$WORKOUT_SESSION_ROUTE/{$WORKOUT_SESSION_ARG_ID}"

    /**
     * Helper function to build the full route with a specific session ID.
     * e.g., Routes.workoutSession("some-uuid-123") -> "workout_session/some-uuid-123"
     */
    fun workoutSession(sessionId: String) = "$WORKOUT_SESSION_ROUTE/$sessionId"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 300 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -300 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -300 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { 300 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        // Register Screen
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack(Routes.LOGIN, inclusive = false)
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Main App with Tab Navigation
        composable(Routes.MAIN) {
            // Pass the NavController down to MainTabScreen so it can navigate deeper
            MainTabScreen(
                navController = navController,
                onSignOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        // --- NEW ---
        // Add the composable for the WorkoutSessionScreen
        composable(
            route = Routes.WORKOUT_SESSION,
            arguments = listOf(navArgument(Routes.WORKOUT_SESSION_ARG_ID) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString(Routes.WORKOUT_SESSION_ARG_ID) ?: ""

            WorkoutSessionScreen(
                sessionId = sessionId,
                onNavigateBack = {
                    // This will now correctly return to the screen that opened the session
                    navController.popBackStack()
                },
                onWorkoutComplete = {
                    // This will also correctly return to the previous screen
                    navController.popBackStack()
                }
            )
        }
    }
}