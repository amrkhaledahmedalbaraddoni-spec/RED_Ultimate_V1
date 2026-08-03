package com.red.sovereign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.red.sovereign.app.RedDashboard
import com.red.sovereign.features.chat.ChatListScreen
import com.red.sovereign.features.calls.CallLogScreen
import com.red.sovereign.features.pstn.DialPadScreen
import com.red.sovereign.features.stories.StoryListSection
import com.red.sovereign.features.profile.SettingsScreen

/**
 * RED Master UI Host
 * The single source of truth for all user interactions.
 */
@Composable
fun RedMainHost(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RED Sovereign", color = Color.Red) },
                actions = { /* Search & More Icons */ }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.Black) {
                val tabs = listOf("Chats", "Status", "Calls", "Phone", "Settings")
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(label) },
                        icon = { /* Tab Icons */ }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> { // Chats + Stories Integration (Like WhatsApp/Telegram)
                    StoryListSection()
                    ChatListScreen { chatId -> navController.navigate("chat/$chatId") }
                }
                1 -> StatusListView()
                2 -> CallLogScreen()
                3 -> DialPadScreen() // System B: Dinstar
                4 -> SettingsScreen()
            }
        }
    }
}
