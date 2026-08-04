package com.red.sovereign.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.red.sovereign.auth.AuthState
import com.red.sovereign.auth.AuthViewModel
import com.red.sovereign.ui.theme.AqyalCyanGlow
import com.red.sovereign.ui.theme.AqyalGold
import com.red.sovereign.ui.theme.AqyalSurfaceNavy

private data class Tab(val label: String, val icon: ImageVector)

@Composable
fun RedDashboard(state: AuthState.Authenticated, viewModel: AuthViewModel) {
    val tabs = listOf(Tab("الرئيسية", Icons.Default.Home), Tab("المحادثات", Icons.Default.Chat), Tab("المكالمات", Icons.Default.Call), Tab("استكشاف", Icons.Default.Explore), Tab("الإعدادات", Icons.Default.Settings))
    var selected by remember { mutableIntStateOf(0) }
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { NavigationBar(containerColor = AqyalSurfaceNavy) { tabs.forEachIndexed { index, tab -> NavigationBarItem(selected == index, { selected = index }, { Icon(tab.icon, tab.label) }, label = { Text(tab.label) }) } } }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("RED", fontSize = 27.sp, color = AqyalGold, fontWeight = FontWeight.Black)
            Text(state.redId, color = AqyalCyanGlow)
            Spacer(Modifier.height(14.dp))
            when (selected) {
                0 -> HomeTab()
                1 -> ChatsTab()
                2 -> CallsTab()
                3 -> ExploreTab()
                else -> SettingsTab(state, viewModel::logout)
            }
        }
    }
}

@Composable private fun HomeTab() = FeatureCard("مرحباً بك في RED", "حسابك معتمد ويعمل دون رقم هاتف. الرسائل والمكالمات الداخلية تستخدم هوية RED المشفرة.")

@Composable private fun ChatsTab() {
    val samples = listOf("فريق RED", "الإدارة المحلية", "مجموعة الأصدقاء")
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("المحادثات", fontSize = 23.sp, fontWeight = FontWeight.Bold) }
        items(samples) { name -> Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Chat, null, tint = AqyalCyanGlow); Column(Modifier.padding(start = 14.dp)) { Text(name, fontWeight = FontWeight.Bold); Text("لا توجد رسائل بعد", color = Color.Gray) } } } }
    }
}

@Composable private fun CallsTab() = Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("طرق الاتصال", fontSize = 23.sp, fontWeight = FontWeight.Bold)
    Text("اختر المسار بوضوح؛ الفيديو لا يمر عبر شريحة DINSTAR.")
    CallAction("صوت RED", "WebRTC مشفر داخل التطبيق", Icons.Default.Call, AqyalCyanGlow)
    CallAction("فيديو RED", "WebRTC عبر SFU المحلي", Icons.Default.Videocam, AqyalCyanGlow)
    CallAction("اتصال يمني", "صوت فقط عبر Asterisk وDINSTAR", Icons.Default.SimCard, AqyalGold)
}

@Composable private fun CallAction(title: String, description: String, icon: ImageVector, color: Color) = Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = color); Column(Modifier.weight(1f).padding(horizontal = 14.dp)) { Text(title, fontWeight = FontWeight.Bold); Text(description, color = Color.Gray) }; Button({}) { Text("فتح") } } }

@Composable private fun ExploreTab() = Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("استكشاف", fontSize = 23.sp, fontWeight = FontWeight.Bold)
    FeatureCard("الحالات", "قصص مشفرة لمدة 24 ساعة — ستتصل بـMinIO المحلي.")
    FeatureCard("المساحات والبث", "مكالمات جماعية وبث عبر mediasoup دون المرور عبر DINSTAR.")
}

@Composable private fun SettingsTab(state: AuthState.Authenticated, logout: () -> Unit) = Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    Text("الإعدادات", fontSize = 23.sp, fontWeight = FontWeight.Bold)
    FeatureCard("هوية RED", "@${state.username}\n${state.redId}")
    FeatureCard("الخادم", "وضع محلي — اتصال آمن عبر الشبكة الداخلية أو WireGuard")
    OutlinedButton(logout, Modifier.fillMaxWidth()) { Text("تسجيل الخروج") }
}

@Composable private fun FeatureCard(title: String, description: String) = Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AqyalGold); Spacer(Modifier.height(6.dp)); Text(description, textAlign = TextAlign.Start) } }
