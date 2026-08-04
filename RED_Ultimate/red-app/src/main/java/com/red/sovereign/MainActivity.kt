package com.red.sovereign

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.red.sovereign.auth.AuthState
import com.red.sovereign.auth.AuthViewModel
import com.red.sovereign.ui.AuthFlow
import com.red.sovereign.ui.RedDashboard
import com.red.sovereign.ui.theme.RedTheme
import com.red.sovereign.ui.theme.SovereignBackground

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedTheme {
                SovereignBackground {
                    val state = authViewModel.state
                    if (state is AuthState.Authenticated) RedDashboard(state, authViewModel)
                    else AuthFlow(authViewModel)
                }
            }
        }
    }
}
