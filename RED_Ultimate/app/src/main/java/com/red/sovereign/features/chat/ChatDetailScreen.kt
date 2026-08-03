package com.red.sovereign.features.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        topBar = { RedChatTopBar(chatId) },
        bottomBar = {
            RedMessageInput(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    viewModel.sendMessage(chatId, inputText)
                    inputText = ""
                },
                onAttach = {}
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(8.dp)) {
            items(messages) { msg ->
                RedMessageBubble(
                    content = msg.content,
                    isMe = msg.isMe,
                    reactions = msg.reactions
                )
                // Status row
                if (msg.isMe) {
                    Row(modifier = Modifier.fillMaxWidth().padding(end = 16.dp), horizontalArrangement = Arrangement.End) {
                        Text(
                            "${msg.status} • ${java.text.SimpleDateFormat("HH:mm").format(java.util.Date(msg.timestamp))}",
                            color = if (msg.status == "READ") Color(0xFF00E5FF) else Color.Gray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
