package com.fitness.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.fitness.app.auth.GoogleAuthCodeStore
import com.fitness.app.auth.UserSession
import com.fitness.app.data.local.AppDatabase
import com.fitness.app.navigation.NavGraph
import com.fitness.app.navigation.Screen
import com.fitness.app.ui.theme.FitnessAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleAuthDeepLink(intent)
        setContent {
            FitnessAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination by produceState<String?>(initialValue = null) {
                        value =
                            withContext(Dispatchers.IO) {
                                val user =
                                    AppDatabase.getInstance(this@MainActivity)
                                        .userDao()
                                        .getUser()
                                if (user != null) {
                                    UserSession.setUser(
                                        name = listOfNotNull(user.name, user.lastName)
                                            .joinToString(" ")
                                            .trim()
                                            .ifBlank { null },
                                        username = user.username,
                                        userId = user.userId,
                                        email = user.email,
                                        picture = user.picture,
                                        bio = user.description
                                    )
                                    "main"
                                } else {
                                    Screen.Login.route
                                }
                            }
                    }
                    if (startDestination == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        NavGraph(
                            navController = navController,
                            startDestination = startDestination!!
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleAuthDeepLink(intent)
    }

    private fun handleAuthDeepLink(intent: Intent?) {
        val data: Uri = intent?.data ?: return
        val path = data.path.orEmpty()
        if (!path.startsWith("/app/auth/callback")) return
        val isProdLink =
            data.scheme == "https" && data.host == "node86.cs.colman.ac.il"
        if (!isProdLink) return
        val accessToken = data.getQueryParameter("code")
        val refreshToken = data.getQueryParameter("refreshToken")
        val userId = data.getQueryParameter("userId")
        android.util.Log.d(
            "MainActivity",
            "Auth deep link received. hasToken=${!accessToken.isNullOrBlank()} tokenLength=${accessToken?.length ?: 0}"
        )
        if (!accessToken.isNullOrBlank()) {
            GoogleAuthCodeStore.setResult(
                com.fitness.app.auth.GoogleAuthResult(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    userId = userId
                )
            )
        }
    }
}
