package com.red.sovereign.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.red.sovereign.auth.AuthState
import com.red.sovereign.auth.AuthViewModel
import com.red.sovereign.auth.PstnState
import com.red.sovereign.social.FeedState
import com.red.sovereign.social.FeedViewModel
import com.red.sovereign.social.Post
import com.red.sovereign.ui.theme.AqyalCyanGlow
import com.red.sovereign.ui.theme.AqyalGold
import com.red.sovereign.ui.theme.AqyalRoyalBlue
import com.red.sovereign.ui.theme.AqyalSurfaceNavy

private enum class MainSection(val label: String, val icon: ImageVector) {
    FEED("المنشورات", Icons.Default.DynamicFeed),
    CHATS("المحادثات", Icons.Default.Forum),
    CREATE("إنشاء", Icons.Default.AddCircle),
    CALLS("المكالمات", Icons.Default.Call),
    PHONE("الهاتف", Icons.Default.Dialpad)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedDashboard(account: AuthState.Authenticated, viewModel: AuthViewModel) {
    var section by remember { mutableStateOf(MainSection.FEED) }
    var showCreate by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val feed: FeedViewModel = viewModel()

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(containerColor = AqyalSurfaceNavy) {
                MainSection.entries.forEach { item ->
                    NavigationBarItem(
                        selected = section == item && item != MainSection.CREATE,
                        onClick = { if (item == MainSection.CREATE) showCreate = true else section = item },
                        icon = {
                            if (item == MainSection.CREATE) {
                                Box(Modifier.size(52.dp).clip(CircleShape).background(AqyalGold), contentAlignment = Alignment.Center) {
                                    Icon(item.icon, item.label, tint = Color.Black, modifier = Modifier.size(31.dp))
                                }
                            } else Icon(item.icon, item.label)
                        },
                        label = { Text(item.label, maxLines = 1, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            RedTopBar(account.redId, onSettings = { showSettings = true })
            when (section) {
                MainSection.FEED -> FeedScreen(account, feed, onCreate = { showCreate = true })
                MainSection.CHATS -> ChatHubScreen()
                MainSection.CALLS -> UnifiedCallsScreen()
                MainSection.PHONE -> DinstarPhoneScreen(account, viewModel)
                MainSection.CREATE -> Unit
            }
        }
    }

    if (showCreate) CreateSheet(
        publishing = feed.state == FeedState.Publishing,
        onDismiss = { showCreate = false },
        onPost = { text -> feed.create(text) { showCreate = false } }
    )
    if (showSettings) SettingsSheet(account, viewModel::logout) { showSettings = false }
}

@Composable
private fun RedTopBar(redId: String, onSettings: () -> Unit) = Row(
    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Text("RED", fontSize = 27.sp, color = AqyalGold, fontWeight = FontWeight.Black)
    Text("  $redId", color = AqyalCyanGlow, fontSize = 11.sp, modifier = Modifier.weight(1f), overflow = TextOverflow.Ellipsis, maxLines = 1)
    IconButton({}, enabled = false) { Icon(Icons.Default.Search, "بحث — قيد الربط") }
    IconButton(onSettings) { Icon(Icons.Default.Settings, "الإعدادات") }
}

@Composable
private fun FeedScreen(account: AuthState.Authenticated, feed: FeedViewModel, onCreate: () -> Unit) {
    var filter by remember { mutableIntStateOf(0) }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            LazyRow(Modifier.padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item { StoryCircle("قصتك", true) }
                items(listOf("صنعاء", "عدن", "فريق RED", "الأصدقاء")) { StoryCircle(it, false) }
            }
        }
        item {
            Row(Modifier.padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("لك", "أتابعهم", "اليمن").forEachIndexed { i, title ->
                    FilterChip(filter == i, {
                        filter = i
                        feed.load(if (i == 2) "LOCAL_YEMEN" else null)
                    }, { Text(if (i == 1) "$title · قريباً" else title) }, enabled = i != 1)
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth().padding(horizontal = 14.dp).clickable(onClick = onCreate), colors = CardDefaults.cardColors(containerColor = AqyalSurfaceNavy)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar("أ"); Text("ماذا يحدث في RED؟", color = Color.LightGray, modifier = Modifier.weight(1f).padding(horizontal = 12.dp)); Icon(Icons.Default.Add, null, tint = AqyalGold)
                }
            }
        }
        when {
            feed.state == FeedState.Loading -> item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AqyalGold) } }
            feed.state is FeedState.Error -> item { EmptyState(Icons.Default.DynamicFeed, "تعذر تحميل نبض RED", (feed.state as FeedState.Error).message) }
            feed.posts.isEmpty() -> item { EmptyState(Icons.Default.DynamicFeed, "ابدأ مجتمع RED", "اكتب أول منشور محلي. النظام يدعم السلاسل والاقتباسات والاستطلاعات، بينما المحتوى الخاص ينتظر تشفير E2EE.") }
            else -> items(feed.posts, key = { it.id }) { PostCard(it, feed::toggleLike) }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun StoryCircle(label: String, own: Boolean) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(Modifier.size(66.dp).clip(CircleShape).background(if (own) AqyalGold else AqyalCyanGlow), contentAlignment = Alignment.Center) {
        Box(Modifier.size(58.dp).clip(CircleShape).background(AqyalRoyalBlue), contentAlignment = Alignment.Center) {
            Icon(if (own) Icons.Default.Add else Icons.Default.Person, null)
        }
    }
    Text(label, fontSize = 11.sp, maxLines = 1)
}

@Composable
private fun PostCard(post: Post, onLike: (Post) -> Unit) = Card(Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
    Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(post.authorDisplayName.take(1)); Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(post.authorDisplayName, fontWeight = FontWeight.Bold)
                Text("@${post.authorUsername} · ${post.authorRedId}", color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            AssistChip({}, { Text(if (post.visibility == "LOCAL_YEMEN") "اليمن" else "عام") }, enabled = false, leadingIcon = { Icon(Icons.Default.Public, null, Modifier.size(15.dp)) })
        }
        Text(post.text, Modifier.padding(vertical = 14.dp), fontSize = 17.sp)
        post.poll?.let { poll -> poll.options.forEach { option -> OutlinedButton({}, Modifier.fillMaxWidth(), enabled = false) { Text("${option.text} · ${option.votes}") } } }
        HorizontalDivider()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            PostAction(Icons.Default.FavoriteBorder, "${post.reactionCounts["LIKE"] ?: 0}", true) { onLike(post) }
            PostAction(Icons.Default.Chat, post.replyCount.toString(), false) {}
            PostAction(Icons.Default.Repeat, post.repostCount.toString(), false) {}
            PostAction(Icons.Default.Share, "مشاركة", false) {}
        }
    }
}

@Composable private fun PostAction(icon: ImageVector, label: String, enabled: Boolean, action: () -> Unit) = TextButton(action, enabled = enabled) { Icon(icon, label, Modifier.size(18.dp)); Text(" $label", fontSize = 11.sp) }
@Composable private fun Avatar(text: String) = Box(Modifier.size(42.dp).clip(CircleShape).background(AqyalGold), contentAlignment = Alignment.Center) { Text(text, color = Color.Black, fontWeight = FontWeight.Black) }

@Composable
private fun ChatHubScreen() {
    var tab by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TabRow(tab) { Tab(tab == 0, { tab = 0 }, text = { Text("الخاص") }, icon = { Icon(Icons.Default.Chat, null) }); Tab(tab == 1, { tab = 1 }, text = { Text("المجموعات") }, icon = { Icon(Icons.Default.Groups, null) }) }
        if (tab == 0) EmptyState(Icons.Default.Chat, "المحادثات الخاصة", "محادثات RED المشفرة ستظهر هنا بعد اكتمال مخزن جلسات libsignal.")
        else EmptyState(Icons.Default.Groups, "المجموعات", "أنشئ مجموعات محلية بأدوار مالك ومسؤول وعضو، ومكالمات جماعية مستقلة.")
    }
}

@Composable
private fun UnifiedCallsScreen() {
    var filter by remember { mutableStateOf("الكل") }
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Text("مركز المكالمات", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("سجل موحد لكل مكالمة من المحادثات والمجموعات والبث والمساحات والهاتف اليمني.", color = Color.LightGray)
        Row(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            RoundCallAction(Icons.Default.Groups, "جماعية", AqyalCyanGlow, false)
            RoundCallAction(Icons.Default.LiveTv, "بث مباشر", Color.Red, false)
            RoundCallAction(Icons.Default.RecordVoiceOver, "مساحات", Color(0xFFA78BFA), false)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            items(listOf("الكل", "فائتة", "صوت", "فيديو", "جماعية", "بث", "مساحات", "DINSTAR")) { title -> FilterChip(filter == title, { filter = title }, { Text(title) }) }
        }
        EmptyState(Icons.Default.History, "لا توجد مكالمات", "ستظهر هنا كل المكالمات مع شارة توضح: RED صوت، RED فيديو، مجموعة، بث، مساحة، أو DINSTAR صوت.")
    }
}

@Composable
private fun RoundCallAction(icon: ImageVector, title: String, color: Color, enabled: Boolean) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    FilledIconButton({}, Modifier.size(62.dp), enabled = enabled) { Icon(icon, title, tint = if (enabled) color else Color.Gray, modifier = Modifier.size(30.dp)) }
    Text(title, fontSize = 11.sp); if (!enabled) Text("قيد الربط", color = Color.Gray, fontSize = 9.sp)
}

@Composable
private fun DinstarPhoneScreen(account: AuthState.Authenticated, viewModel: AuthViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        Card(Modifier.fillMaxWidth().padding(horizontal = 14.dp), colors = CardDefaults.cardColors(containerColor = AqyalGold.copy(alpha = .14f))) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SimCard, null, tint = AqyalGold, modifier = Modifier.size(35.dp)); Column(Modifier.padding(start = 12.dp)) {
                    Text("الهاتف اليمني عبر DINSTAR", fontWeight = FontWeight.Bold, color = AqyalGold)
                    Text(if (account.pstnEnabled) "مصرح لك — مكالمات صوتية فقط" else "غير مفعل — يفعله المسؤول من اللوحة", fontSize = 12.sp)
                }
            }
        }
        TabRow(tab) {
            listOf(Icons.Default.Dialpad to "الأرقام", Icons.Default.Star to "المفضلة", Icons.Default.History to "السجل", Icons.Default.Contacts to "جهات الاتصال").forEachIndexed { i, item -> Tab(tab == i, { tab = i }, icon = { Icon(item.first, null) }, text = { Text(item.second, fontSize = 10.sp) }) }
        }
        when (tab) {
            0 -> DialPad(account.pstnEnabled, viewModel)
            1 -> EmptyState(Icons.Default.Star, "المفضلة", "أرقامك اليمنية المفضلة")
            2 -> EmptyState(Icons.Default.History, "سجل DINSTAR", "المكالمات الهاتفية فقط؛ السجل الموحد موجود في مركز المكالمات")
            else -> EmptyState(Icons.Default.Contacts, "جهات الاتصال", "اختيار رقم يمني للاتصال الصوتي")
        }
    }
}

@Composable
private fun DialPad(enabled: Boolean, viewModel: AuthViewModel) {
    var number by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(number.ifEmpty { "أدخل الرقم" }, fontSize = 27.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            IconButton({ if (number.isNotEmpty()) number = number.dropLast(1) }) { Icon(Icons.Default.Backspace, "حذف") }
        }
        listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf("*","0","#")).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { row.forEach { digit -> FilledIconButton({ number += digit }, Modifier.size(64.dp)) { Text(digit, fontSize = 23.sp) } } }
        }
        Button({ viewModel.clearPstnState(); viewModel.dialPstn(number) }, enabled = enabled && number.filter(Char::isDigit).length >= 6, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Call, null); Text(" اتصال صوتي عبر DINSTAR") }
        when (val state = viewModel.pstnState) {
            PstnState.Dialing -> CircularProgressIndicator(color = AqyalGold)
            is PstnState.Started -> Text("بدأ الاتصال · ${state.usedToday}/${state.dailyLimit} اليوم", color = AqyalGold)
            is PstnState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            PstnState.Idle -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateSheet(publishing: Boolean, onDismiss: () -> Unit, onPost: (String) -> Unit) {
    var composer by remember { mutableStateOf(false) }; var text by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("إنشاء في RED", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            if (composer) {
                OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth().height(150.dp), placeholder = { Text("اكتب منشوراً، سلسلة، أو فكرة طويلة…") })
                Button({ if (text.isNotBlank()) onPost(text.trim()) }, Modifier.fillMaxWidth(), enabled = text.isNotBlank() && !publishing) { if (publishing) CircularProgressIndicator(Modifier.size(20.dp)) else Text("نشر محلي") }
            } else {
                CreateOption(Icons.Default.DynamicFeed, "منشور أو سلسلة", "نص طويل، اقتباس، استطلاع، صور وفيديو", true) { composer = true }
                CreateOption(Icons.Default.AddCircle, "حالة 24 ساعة", "صورة أو فيديو قصير — قيد الربط", false) {}
                CreateOption(Icons.Default.LiveTv, "بث مباشر", "فيديو عبر SFU المحلي — قيد الربط", false) {}
                CreateOption(Icons.Default.RecordVoiceOver, "مساحة صوتية", "مؤتمر عام مثل Spaces — قيد الربط", false) {}
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable private fun CreateOption(icon: ImageVector, title: String, detail: String, enabled: Boolean, click: () -> Unit) = Card(Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = click)) { Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = if (enabled) AqyalGold else Color.Gray, modifier = Modifier.size(31.dp)); Column(Modifier.padding(horizontal = 14.dp)) { Text(title, fontWeight = FontWeight.Bold, color = if (enabled) Color.Unspecified else Color.Gray); Text(detail, color = Color.Gray, fontSize = 12.sp) } } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun SettingsSheet(account: AuthState.Authenticated, logout: () -> Unit, dismiss: () -> Unit) = ModalBottomSheet(onDismissRequest = dismiss) {
    Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("الإعدادات", fontSize = 25.sp, fontWeight = FontWeight.Bold); FeatureCard("هوية RED", "@${account.username}\n${account.redId}")
        FeatureCard("الخصوصية", "مفاتيح الهوية داخل Android Keystore · لا رقم هاتف · لا SMS")
        FeatureCard("الخادم", "Local-first عبر الشبكة الداخلية أو WireGuard")
        OutlinedButton(logout, Modifier.fillMaxWidth()) { Text("تسجيل الخروج") }; Spacer(Modifier.height(22.dp))
    }
}

@Composable private fun EmptyState(icon: ImageVector, title: String, detail: String) = Column(Modifier.fillMaxWidth().padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = AqyalGold, modifier = Modifier.size(62.dp)); Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(detail, textAlign = TextAlign.Center, color = Color.Gray, modifier = Modifier.padding(top = 8.dp)) }
@Composable private fun FeatureCard(title: String, detail: String) = Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text(title, color = AqyalGold, fontWeight = FontWeight.Bold); Text(detail) } }
