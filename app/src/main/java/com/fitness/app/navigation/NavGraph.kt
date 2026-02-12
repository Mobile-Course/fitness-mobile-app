package com.fitness.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fitness.app.ui.screens.login.LoginScreen
import com.fitness.app.ui.screens.signup.SignupScreen
import com.fitness.app.ui.screens.main.MainScreen
// import com.fitness.app.ui.screens.preferences.PreferencesScreen

@Composable
fun NavGraph(navController: NavHostController, startDestination: String) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                }
            )
        }
        composable(Screen.Signup.route) {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate("main") {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable("main") {
            MainScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Preferences.route) {
            // PreferencesScreen(
            //     onBack = {
            //         navController.popBackStack()
            //     },
            //     onLogout = {
            //         navController.navigate(Screen.Login.route) {
            //             popUpTo(0) { inclusive = true }
            //         }
            //     }
            // )
        }
    }
}
