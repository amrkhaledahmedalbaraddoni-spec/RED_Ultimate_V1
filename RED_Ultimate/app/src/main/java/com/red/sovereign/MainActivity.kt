package com.red.sovereign

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.red.sovereign.ui.RedMainHost
import com.red.sovereign.features.auth.RedSplashScreen
import com.red.sovereign.features.auth.SovereignAuthScreensKt
import com.red.sovereign.features.chat.ChatDetailScreen
import com.red.sovereign.core.theme.REDTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            REDTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") { RedSplashScreen { navController.navigate("auth") } }
                    composable("auth") { /* Login/Register Screen */ }
                    composable("main") { RedMainHost(navController) }
                    composable("chat/{id}") { backStack ->
                        ChatDetailScreen(backStack.arguments?.getString("id") ?: "")
                    }
                }
            }
        }
    }
}
