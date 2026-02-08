package com.fitness.app.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fitness.app.navigation.Screen
import com.fitness.app.ui.screens.ai_tips.AITipsScreen
import com.fitness.app.ui.screens.feed.FeedScreen
// import com.fitness.app.ui.screens.profile.ProfileScreen

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Feed : BottomNavItem(
        Screen.Feed.route,
        "Feed",
        Icons.Filled.Home,
        Icons.Outlined.Home
    )
    object Post : BottomNavItem(
        Screen.Post.route,
        "Post",
        Icons.Filled.AddCircle,
        Icons.Outlined.AddCircleOutline
    )
    object AITips : BottomNavItem(
        Screen.AITips.route,
        "AI Tips",
        Icons.Filled.AutoAwesome,
        Icons.Outlined.AutoAwesome
    )
    object Profile : BottomNavItem(
        Screen.Profile.route,
        "Profile",
        Icons.Filled.Person,
        Icons.Outlined.PersonOutline
    )
}

@Composable
fun MainScreen(onNavigateToSettings: () -> Unit) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Feed,
        BottomNavItem.Post,
        BottomNavItem.AITips,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFF3F4F6)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Feed.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Feed.route) {
                FeedScreen()
            }
            composable(Screen.Post.route) {
                Text("Post Screen Placeholder")
            }
            composable(Screen.AITips.route) {
                AITipsScreen()
            }
            composable(Screen.Profile.route) {
                // ProfileScreen(onNavigateToSettings = onNavigateToSettings)
            }
        }
    }
}
