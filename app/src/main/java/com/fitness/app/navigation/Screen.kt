package com.fitness.app.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Feed : Screen("feed")
    object Profile : Screen("profile")
    object Post : Screen("post")
    object Preferences : Screen("preferences")
    object AITips : Screen("ai_tips")
    object Signup : Screen("signup")
    object EditProfile : Screen("edit_profile")
}
