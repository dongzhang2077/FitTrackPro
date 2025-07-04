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

// Navigation routes
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home" // For future use
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
                    // TODO: Navigate to home screen when implemented
                    // For now, just show a placeholder
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

        // TODO: Add Home screen and other screens here
        // composable(Routes.HOME) {
        //     HomeScreen()
        // }
    }
}