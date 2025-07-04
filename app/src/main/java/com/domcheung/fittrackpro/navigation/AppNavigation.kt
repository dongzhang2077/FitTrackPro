package com.domcheung.fittrackpro.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.domcheung.fittrackpro.presentation.login.LoginScreen
import com.domcheung.fittrackpro.presentation.register.RegisterScreen
import com.domcheung.fittrackpro.presentation.main.MainTabScreen

// Navigation routes
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"     // New main app with tabs
    const val HOME = "home"     // Individual tab - for future deep linking
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        // Add smooth enter/exit animations
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 300 }, // Slide in from right
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -300 }, // Slide out to left
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -300 }, // Slide in from left when going back
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { 300 }, // Slide out to right when going back
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // Login Screen
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // Navigate to main app after successful login
                    navController.navigate(Routes.MAIN) {
                        // Clear login from back stack so user can't go back
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
                    // Navigate back to login screen after successful registration
                    navController.popBackStack(Routes.LOGIN, inclusive = false)
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Main App with Tab Navigation
        composable(Routes.MAIN) {
            MainTabScreen(
                onSignOut = {
                    // Navigate back to login and clear main from back stack
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        // TODO: Add individual tab routes for deep linking
        // composable(Routes.HOME) { ... }
        // composable("workout/{planId}") { ... }
        // composable("progress/{timeRange}") { ... }
    }
}