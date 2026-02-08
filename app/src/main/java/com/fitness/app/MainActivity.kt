package com.fitness.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.fitness.app.auth.GoogleAuthCodeStore
import com.fitness.app.navigation.NavGraph
import com.fitness.app.ui.theme.FitnessAppTheme

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
                    NavGraph(navController = navController)
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
        if (data.scheme != "https") return
        if (data.host != "node86.cs.colman.ac.il") return
        if (!data.path.orEmpty().startsWith("/api/auth/google/redirect")) return
        val code = data.getQueryParameter("code")
        if (!code.isNullOrBlank()) {
            GoogleAuthCodeStore.setCode(code)
        }
    }
}
