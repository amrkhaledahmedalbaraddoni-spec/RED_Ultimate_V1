package com.red.sovereign.features.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LuxuryChatBubble(message: String, isMe: Boolean, status: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMe) Color(0xFFB71C1C) else Color(0xFF1E1E1E),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(message, color = Color.White, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        java.text.SimpleDateFormat("HH:mm").format(java.util.Date()),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    if (isMe) {
                        Text(
                            when (status) {
                                "SENDING" -> "○"
                                "SENT" -> "✓"
                                "DELIVERED" -> "✓✓"
                                "READ" -> "✓✓"
                                else -> status
                            },
                            color = if (status == "READ") Color(0xFF00E5FF) else Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = if (status == "READ") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RedChatBubble(content: String, isMe: Boolean, status: String) {
    LuxuryChatBubble(message = content, isMe = isMe, status = status)
}

@Composable
fun RedMessageBubble(content: String, isMe: Boolean, reactions: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMe) Color(0xFFB71C1C) else Color(0xFF1E1E1E),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(content, color = Color.White, modifier = Modifier.padding(12.dp))
        }
        if (reactions.isNotEmpty()) {
            Row(modifier = Modifier.padding(top = 2.dp)) {
                reactions.forEach { emoji ->
                    Surface(
                        color = Color(0xFF2C2C2C),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 2.dp)
                    ) {
                        Text(emoji, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RedChatTopBar(chatId: String) {
    Surface(color = Color(0xFF121212), tonalElevation = 4.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp).background(Color(0xFFD32F2F), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                Text(chatId.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("RED Squad $chatId", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("● Sovereign • Encrypted • Online", color = Color(0xFF4CAF50), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun RedMessageInput(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit, onAttach: () -> Unit) {
    Surface(tonalElevation = 8.dp, color = Color(0xFF1A1A1A)) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onAttach) { Text("📎", fontSize = 20.sp) }
            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("RED Encrypted Message", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2C2C2C),
                    unfocusedContainerColor = Color(0xFF2C2C2C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.material3.FloatingActionButton(
                onClick = onSend,
                containerColor = Color(0xFFD32F2F),
                modifier = Modifier.size(48.dp)
            ) {
                Text("➤", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun ChatInputBar(onSend: (String) -> Unit = {}) {
    var text by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    RedMessageInput(text = text, onTextChange = { text = it }, onSend = { onSend(text); text = "" }, onAttach = {})
}
