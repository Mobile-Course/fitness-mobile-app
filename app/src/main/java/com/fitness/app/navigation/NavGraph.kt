package com.fitness.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.Text

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            // Placeholder for LoginScreen
            Text("Login Screen")
        }
        composable(Screen.Feed.route) {
            // Placeholder for FeedScreen
            Text("Feed Screen")
        }
        composable(Screen.Profile.route) {
            // Placeholder for ProfileScreen
            Text("Profile Screen")
        }
        composable(Screen.Post.route) {
            // Placeholder for PostScreen
            Text("Post Screen")
        }
        composable(Screen.Preferences.route) {
            // Placeholder for PreferencesScreen
            Text("Preferences Screen")
        }
        composable(Screen.AITips.route) {
            // Placeholder for AITipsScreen
            Text("AI Tips Screen")
        }
    }
}
