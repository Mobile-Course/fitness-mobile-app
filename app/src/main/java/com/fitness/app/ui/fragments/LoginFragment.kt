package com.fitness.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fitness.app.ui.screens.login.LoginScreen
import com.fitness.app.ui.theme.FitnessAppTheme

class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FitnessAppTheme {
                    LoginScreen(
                        onLoginSuccess = {
                            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToMainFragment())
                        },
                        onNavigateToSignup = {
                            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSignupFragment())
                        }
                    )
                }
            }
        }
    }
}
