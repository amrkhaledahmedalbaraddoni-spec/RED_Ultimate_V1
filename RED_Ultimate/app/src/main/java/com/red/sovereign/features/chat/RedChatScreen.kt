package com.red.sovereign.features.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChatPreview(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unread: Int = 0,
    val isOnline: Boolean = false,
    val avatarColor: Color = Color(0xFFD32F2F)
)

@Composable
fun RedChatScreen(onChatClick: (String) -> Unit) {
    val chats = remember {
        listOf(
            ChatPreview("1", "RED Master", "🔴 Sovereign system online", "12:34", 2, true),
            ChatPreview("2", "DINSTAR Control", "SIM Slot 3: READY 85%", "11:20", 0, true, Color(0xFF4CAF50)),
            ChatPreview("3", "VoIP Conference", "Conference 1080p AV1 active", "09:45", 5, true, Color(0xFF2196F3)),
            ChatPreview("4", "Guaranteed Delivery", "UUID v7 test ✓✓ READ", "Yesterday", 0, false, Color(0xFFFF9800)),
            ChatPreview("5", "PSTN Gateway", "Yemen Mobile: Call ended 02:34", "Yesterday", 1, true, Color(0xFF9C27B0)),
        )
    }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF0A0A0A), tonalElevation = 4.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFD32F2F)), contentAlignment = Alignment.Center) {
                            Text("R", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("RED Sovereign", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("100% Local • Encrypted • ${chats.sumOf { it.unread }} unread", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF0A0A0A),
        floatingActionButton = {
            FloatingActionButton(onClick = { onChatClick("new") }, containerColor = Color(0xFFD32F2F)) {
                Text("+", color = Color.White, fontSize = 24.sp)
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(chats) { chat ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onChatClick(chat.id) }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(chat.avatarColor), contentAlignment = Alignment.Center) {
                            Text(chat.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        if (chat.isOnline) {
                            Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(Color(0xFF4CAF50)).align(Alignment.BottomEnd))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(chat.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(chat.time, color = Color.Gray, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(chat.lastMessage, color = Color.Gray, fontSize = 14.sp, maxLines = 1, modifier = Modifier.weight(1f))
                            if (chat.unread > 0) {
                                Surface(color = Color(0xFFD32F2F), shape = RoundedCornerShape(12.dp)) {
                                    Text("${chat.unread}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
                Divider(color = Color(0xFF1E1E1E), thickness = 0.5.dp, modifier = Modifier.padding(start = 74.dp))
            }
        }
    }
}

// Legacy wrappers for compatibility
@Composable
fun RedChatTopBar(chatId: String) {
    Surface(color = Color(0xFF121212)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFD32F2F)), contentAlignment = Alignment.Center) {
                Text(chatId.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("RED Squad $chatId", color = Color.White, fontWeight = FontWeight.Bold)
                Text("● Sovereign • Encrypted • Online", color = Color(0xFF4CAF50), fontSize = 12.sp)
            }
        }
    }
}
