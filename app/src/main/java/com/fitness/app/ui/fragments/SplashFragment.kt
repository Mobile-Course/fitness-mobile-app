package com.fitness.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fitness.app.auth.UserSession
import com.fitness.app.data.local.AppDatabase
import com.fitness.app.ui.theme.FitnessAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FitnessAppTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkSession()
    }

    private fun checkSession() {
        lifecycleScope.launch {
            val nextDestination = withContext(Dispatchers.IO) {
                val context = requireContext()
                val user = AppDatabase.getInstance(context).userDao().getUser()
                if (user != null) {
                    val refreshToken = user.refreshToken?.trim()
                    if (refreshToken.isNullOrBlank()) {
                        AppDatabase.getInstance(context).userDao().clear()
                        UserSession.clearPersistedAccessToken(context)
                        UserSession.clear()
                        "login"
                    } else {
                        UserSession.restoreAccessToken(context)
                        UserSession.setUser(
                            name = user.name,
                            username = user.username,
                            userId = user.userId,
                            email = user.email,
                            picture = user.picture,
                            bio = user.description,
                            sportType = user.sportType,
                            streak = user.streak,
                            refreshToken = refreshToken
                        )
                        "main"
                    }
                } else {
                    "login"
                }
            }

            if (nextDestination == "main") {
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToMainFragment())
            } else {
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLoginFragment())
            }
        }
    }
}
