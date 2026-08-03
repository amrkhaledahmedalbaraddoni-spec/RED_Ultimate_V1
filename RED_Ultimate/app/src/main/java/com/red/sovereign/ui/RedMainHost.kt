package com.red.sovereign.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.red.sovereign.features.chat.ChatDetailScreen
import com.red.sovereign.features.chat.RedChatScreen
import com.red.sovereign.features.pstn.DialPadScreen

/**
 * RED Ultimate Main Host - Sovereign Navigation
 * System A: VoIP, System B: PSTN, System C: Messaging
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedMainHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "chat_list") {
        composable("chat_list") {
            RedChatScreen(
                onChatClick = { chatId ->
                    navController.navigate("chat/$chatId")
                }
            )
        }
        composable("chat/{id}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("id") ?: "unknown"
            ChatDetailScreen(chatId = chatId)
        }
        composable("dial_pad") {
            DialPadScreen(onNavigateToCall = { number ->
                navController.navigate("call/$number")
            })
        }
        composable("call/{number}") { backStackEntry ->
            val number = backStackEntry.arguments?.getString("number") ?: ""
            // Call screen would be here - System A/B
            Text("Calling $number via RED Sovereign - System A/B")
        }
    }
}
