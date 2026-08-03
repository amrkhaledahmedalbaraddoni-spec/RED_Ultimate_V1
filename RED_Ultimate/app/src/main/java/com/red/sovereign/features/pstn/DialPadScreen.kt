package com.red.sovereign.features.pstn

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.red.sovereign.features.calls.YemeniOperatorDetector

@Composable
fun DialPadScreen(onNavigateToCall: (String) -> Unit) {
    var number by remember { mutableStateOf("") }
    val opInfo = remember(number) { if (number.length >= 2) YemeniOperatorDetector.getOperatorInfo(number) else null }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Operator Badge
        if (opInfo != null && number.length >= 2) {
            BadgedBox(badge = {
                Badge(containerColor = opInfo.brandColor) { Text(opInfo.code, color = Color.White, fontSize = 10.sp) }
            }) {
                Surface(color = opInfo.brandColor.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium) {
                    Text(opInfo.name, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = if (number.isEmpty()) "Enter number" else number,
            fontSize = 36.sp,
            modifier = Modifier.padding(vertical = 24.dp),
            color = if (number.isEmpty()) Color.Gray else Color.White,
            fontWeight = FontWeight.Light
        )

        // Keypad
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(keys) { key ->
                ElevatedButton(
                    onClick = { if (number.length < 20) number += key },
                    modifier = Modifier.size(80.dp),
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Text(key, fontSize = 28.sp, color = Color.White)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (number.isNotEmpty()) number = number.dropLast(1) }, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(28.dp))
            }

            FloatingActionButton(
                onClick = { if (number.isNotEmpty()) onNavigateToCall(number) },
                containerColor = Color(0xFFF57C00),
                modifier = Modifier.size(72.dp)
            ) {
                Icon(Icons.Default.Call, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.size(56.dp))
        }
    }
}

// Legacy no-arg version for compatibility
@Composable
fun DialPadScreen() {
    DialPadScreen(onNavigateToCall = {})
}
