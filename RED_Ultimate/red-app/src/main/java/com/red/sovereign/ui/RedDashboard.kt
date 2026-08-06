package com.red.sovereign.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.red.sovereign.R
import com.red.sovereign.auth.AuthState
import com.red.sovereign.auth.AuthViewModel
import com.red.sovereign.auth.PstnState
import com.red.sovereign.calls.CallHistoryItem
import com.red.sovereign.calls.CallHistoryViewModel
import com.red.sovereign.contacts.DirectoryState
import com.red.sovereign.contacts.DirectoryViewModel
import com.red.sovereign.contacts.PublicRedProfile
import com.red.sovereign.core.RedConnectionService
import com.red.sovereign.crypto.DecryptedMessage
import com.red.sovereign.crypto.DecryptedMessageBus
import com.red.sovereign.crypto.SafetyQrScanner
import com.red.sovereign.crypto.SafetyState
import com.red.sovereign.crypto.SafetyViewModel
import com.red.sovereign.groups.GroupState
import com.red.sovereign.groups.GroupViewModel
import com.red.sovereign.media.AttachmentManifest
import com.red.sovereign.media.AttachmentState
import com.red.sovereign.media.AttachmentViewModel
import com.red.sovereign.social.FeedState
import com.red.sovereign.social.FeedViewModel
import com.red.sovereign.social.Post
import com.red.sovereign.social.ThreadState
import com.red.sovereign.stories.Story
import com.red.sovereign.stories.StoryState
import com.red.sovereign.stories.StoryVideoPlayer
import com.red.sovereign.stories.StoryViewerState
import com.red.sovereign.stories.StoryViewModel
import com.red.sovereign.ui.theme.AqyalCyanGlow
import com.red.sovereign.ui.theme.AqyalGold
import com.red.sovereign.ui.theme.AqyalRoyalBlue
import com.red.sovereign.ui.theme.AqyalSurfaceNavy
import com.red.sovereign.ui.theme.AqyalSurfaceRaised
import com.red.sovereign.ui.theme.YounesEmerald
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.security.MessageDigest

private enum class MainSection(val label: String, val icon: ImageVector) {
    HOME("الرئيسية", Icons.Default.Home),
    CHATS("الدردشات", Icons.Default.Forum),
    GROUPS("المجموعات", Icons.Default.Groups),
    CALLS("المكالمات", Icons.Default.Call),
    MORE("المزيد", Icons.Default.Menu)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedDashboard(account: AuthState.Authenticated, viewModel: AuthViewModel) {
    var section by remember { mutableStateOf(MainSection.HOME) }
    var showCreate by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showDinstar by remember { mutableStateOf(false) }
    val feed: FeedViewModel = viewModel()
    val stories: StoryViewModel = viewModel()
    val groups: GroupViewModel = viewModel()
    val directory: DirectoryViewModel = viewModel()
    val safety: SafetyViewModel = viewModel()
    val attachments: AttachmentViewModel = viewModel()
    val callHistory: CallHistoryViewModel = viewModel()
    val createStoryPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(stories::upload) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (section == MainSection.HOME && !showDinstar) FloatingActionButton(
                onClick = { showCreate = true },
                containerColor = YounesEmerald,
                contentColor = Color(0xFF002117)
            ) { Icon(Icons.Default.Add, "إنشاء محتوى") }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .98f)) {
                MainSection.entries.forEach { item ->
                    NavigationBarItem(
                        selected = section == item,
                        onClick = { section = item; showDinstar = false; if (item == MainSection.CALLS) callHistory.load() },
                        icon = { Icon(item.icon, item.label) },
                        label = { Text(item.label, maxLines = 1, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            RedTopBar(account.redId, onSettings = { showSettings = true })
            when {
                showDinstar -> DinstarPhoneScreen(account, viewModel)
                section == MainSection.HOME -> FeedScreen(account, feed, stories, onCreate = { showCreate = true })
                section == MainSection.CHATS -> ChatHubScreen(account, groups, directory, safety, attachments, showGroups = false)
                section == MainSection.GROUPS -> ChatHubScreen(account, groups, directory, safety, attachments, showGroups = true)
                section == MainSection.CALLS -> UnifiedCallsScreen(callHistory)
                else -> MoreScreen(account, onDinstar = { showDinstar = true }, onSettings = { showSettings = true }, onContacts = { section = MainSection.CHATS })
            }
        }
    }

    if (showCreate) CreateSheet(
        publishing = feed.state == FeedState.Publishing,
        onDismiss = { showCreate = false },
        onPost = { text -> feed.create(text) { showCreate = false } },
        onStory = { showCreate = false; createStoryPicker.launch(arrayOf("image/*", "video/*")) }
    )
    if (showSettings) SettingsSheet(account, viewModel::logout) { showSettings = false }
}

@Composable
private fun RedTopBar(redId: String, onSettings: () -> Unit) = Row(
    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Image(
        painterResource(R.drawable.younes_icon_master),
        contentDescription = "يونس",
        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop
    )
    Text(" يونس", fontSize = 24.sp, color = AqyalGold, fontWeight = FontWeight.Black)
    Text("  $redId", color = AqyalCyanGlow, fontSize = 11.sp, modifier = Modifier.weight(1f), overflow = TextOverflow.Ellipsis, maxLines = 1)
    IconButton({}, enabled = false) { Icon(Icons.Default.Search, "بحث — قيد الربط") }
    IconButton(onSettings) { Icon(Icons.Default.Settings, "الإعدادات") }
}

@Composable
private fun FeedScreen(account: AuthState.Authenticated, feed: FeedViewModel, stories: StoryViewModel, onCreate: () -> Unit) {
    var filter by remember { mutableIntStateOf(0) }
    var threadPost by remember { mutableStateOf<Post?>(null) }
    var quotePost by remember { mutableStateOf<Post?>(null) }
    var replyText by remember { mutableStateOf("") }
    var quoteText by remember { mutableStateOf("") }
    val storyPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(stories::upload) }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            LazyRow(Modifier.padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item { StoryCircle(if (stories.state == StoryState.Uploading) "يرفع…" else "قصتك", true) { storyPicker.launch(arrayOf("image/*", "video/*")) } }
                items(stories.stories, key = Story::id) { story -> StoryCircle(story.ownerDisplayName, false) { stories.open(story) } }
            }
        }
        item {
            Row(Modifier.padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("لك", "أتابعهم", "اليمن").forEachIndexed { i, title ->
                    FilterChip(filter == i, {
                        filter = i
                        feed.load(when (i) { 1 -> "FOLLOWING"; 2 -> "YEMEN"; else -> null })
                    }, { Text(title) })
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth().padding(horizontal = 14.dp).clickable(onClick = onCreate), colors = CardDefaults.cardColors(containerColor = AqyalSurfaceNavy)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar("أ"); Text("ماذا يحدث في يونس؟", color = Color.LightGray, modifier = Modifier.weight(1f).padding(horizontal = 12.dp)); Icon(Icons.Default.Add, null, tint = AqyalGold)
                }
            }
        }
        if (feed.state is FeedState.Message) item { Text((feed.state as FeedState.Message).text, color = AqyalGold, modifier = Modifier.padding(horizontal = 18.dp)) }
        when {
            feed.state == FeedState.Loading -> item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AqyalGold) } }
            feed.state is FeedState.Error -> item { EmptyState(Icons.Default.DynamicFeed, "تعذر تحميل نبض يونس", (feed.state as FeedState.Error).message) }
            feed.posts.isEmpty() -> item { EmptyState(Icons.Default.DynamicFeed, "ابدأ مجتمع يونس", "اكتب أول منشور محلي. النظام يدعم السلاسل والاقتباسات والاستطلاعات، بينما المحتوى الخاص ينتظر تشفير E2EE.") }
            else -> items(feed.posts, key = { it.id }) { post -> PostCard(post, account.redId, feed::toggleLike, feed::follow, feed::vote, { threadPost = post; feed.loadThread(post) }, { quotePost = post }) }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
    threadPost?.let { root ->
        AlertDialog(
            onDismissRequest = { threadPost = null; replyText = ""; feed.closeThread() },
            title = { Text("سلسلة يونس") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (val threadState = feed.threadState) {
                        ThreadState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally), color = AqyalGold)
                        is ThreadState.Error -> Text(threadState.message, color = MaterialTheme.colorScheme.error)
                        else -> LazyColumn(Modifier.height(300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(feed.threadPosts, key = { it.id }) { item ->
                                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (item.id == root.id) AqyalSurfaceRaised else AqyalSurfaceNavy)) {
                                    Column(Modifier.padding(12.dp)) { Text("@${item.authorUsername} · ${item.authorRedId}", color = AqyalCyanGlow, fontSize = 10.sp); Text(item.text) }
                                }
                            }
                        }
                    }
                    OutlinedTextField(replyText, { replyText = it }, Modifier.fillMaxWidth(), placeholder = { Text("اكتب ردًا علنيًا في نبض يونس…") }, maxLines = 4)
                    Button({ feed.reply(root, replyText) { replyText = "" } }, Modifier.fillMaxWidth(), enabled = replyText.isNotBlank() && feed.threadState != ThreadState.Publishing) { Text("إرسال الرد") }
                }
            },
            confirmButton = { TextButton({ threadPost = null; replyText = ""; feed.closeThread() }) { Text("إغلاق") } }
        )
    }
    quotePost?.let { quoted ->
        AlertDialog(
            onDismissRequest = { quotePost = null; quoteText = "" },
            title = { Text("اقتباس منشور @${quoted.authorUsername}") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Card { Text(quoted.text, Modifier.padding(12.dp), color = Color.Gray) }; OutlinedTextField(quoteText, { quoteText = it }, Modifier.fillMaxWidth(), label = { Text("تعليقك") }, maxLines = 5) } },
            confirmButton = { Button({ feed.quote(quoted, quoteText) { quotePost = null; quoteText = "" } }, enabled = quoteText.isNotBlank() && feed.state != FeedState.Publishing) { Text("نشر الاقتباس") } },
            dismissButton = { TextButton({ quotePost = null; quoteText = "" }) { Text("إلغاء") } }
        )
    }
    val viewer = stories.viewer
    if (viewer !is StoryViewerState.Closed) {
        val story = when (viewer) {
            is StoryViewerState.Loading -> viewer.story
            is StoryViewerState.Image -> viewer.story
            is StoryViewerState.Video -> viewer.story
            is StoryViewerState.Unsupported -> viewer.story
            is StoryViewerState.Error -> viewer.story
            StoryViewerState.Closed -> error("unreachable")
        }
        AlertDialog(
            onDismissRequest = stories::closeViewer,
            title = { Text(story.ownerDisplayName) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (viewer) {
                        is StoryViewerState.Loading -> Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AqyalGold) }
                        is StoryViewerState.Image -> Image(viewer.image, story.caption ?: "حالة يونس", Modifier.fillMaxWidth().height(360.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Fit)
                        is StoryViewerState.Video -> StoryVideoPlayer(viewer.uri, Modifier.fillMaxWidth().height(360.dp).clip(RoundedCornerShape(14.dp)))
                        is StoryViewerState.Unsupported -> Text(viewer.message, color = Color.Gray)
                        is StoryViewerState.Error -> Text("تعذر عرض الحالة: ${viewer.message}", color = MaterialTheme.colorScheme.error)
                        StoryViewerState.Closed -> Unit
                    }
                    if (!story.caption.isNullOrBlank()) Text(story.caption)
                    Text("المشاهدات: ${story.viewCount}", color = Color.Gray, fontSize = 12.sp)
                }
            },
            confirmButton = { Button(stories::closeViewer) { Text("إغلاق") } }
        )
    }
}

@Composable
private fun StoryCircle(label: String, own: Boolean, click: () -> Unit) = Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = click)) {
    Box(Modifier.size(66.dp).clip(CircleShape).background(if (own) AqyalGold else AqyalCyanGlow), contentAlignment = Alignment.Center) {
        Box(Modifier.size(58.dp).clip(CircleShape).background(AqyalRoyalBlue), contentAlignment = Alignment.Center) {
            Icon(if (own) Icons.Default.Add else Icons.Default.Person, null)
        }
    }
    Text(label, fontSize = 11.sp, maxLines = 1)
}

@Composable
private fun PostCard(
    post: Post,
    currentRedId: String,
    onLike: (Post) -> Unit,
    onFollow: (Post) -> Unit,
    onVote: (Post, String) -> Unit,
    onThread: () -> Unit,
    onQuote: () -> Unit
) = Card(Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
    Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(post.authorDisplayName.take(1)); Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(post.authorDisplayName, fontWeight = FontWeight.Bold)
                Text("@${post.authorUsername} · ${post.authorRedId}", color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (post.authorRedId != currentRedId) TextButton({ onFollow(post) }) { Text("متابعة") }
            AssistChip({}, { Text(if (post.visibility == "LOCAL_YEMEN") "اليمن" else "عام") }, enabled = false, leadingIcon = { Icon(Icons.Default.Public, null, Modifier.size(15.dp)) })
        }
        Text(post.text, Modifier.padding(vertical = 14.dp), fontSize = 17.sp)
        post.quotePostId?.let { quotedId -> AssistChip({}, { Text("اقتباس يونس · ${quotedId.take(8)}") }, enabled = false, leadingIcon = { Icon(Icons.Default.Repeat, null, Modifier.size(15.dp)) }) }
        post.poll?.let { poll ->
            val totalVotes = poll.options.sumOf { it.votes }.coerceAtLeast(1)
            poll.options.forEach { option ->
                val percent = (option.votes * 100 / totalVotes).toInt()
                OutlinedButton({ onVote(post, option.id) }, Modifier.fillMaxWidth()) {
                    Text("${option.text} · ${option.votes} ($percent%)")
                }
            }
        }
        HorizontalDivider()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            PostAction(Icons.Default.FavoriteBorder, "${post.reactionCounts["LIKE"] ?: 0}", true) { onLike(post) }
            PostAction(Icons.Default.Chat, post.replyCount.toString(), true, onThread)
            PostAction(Icons.Default.Repeat, "اقتباس", true, onQuote)
            PostAction(Icons.Default.Share, "مشاركة", false) {}
        }
    }
}

@Composable private fun PostAction(icon: ImageVector, label: String, enabled: Boolean, action: () -> Unit) = TextButton(action, enabled = enabled) { Icon(icon, label, Modifier.size(18.dp)); Text(" $label", fontSize = 11.sp) }
@Composable private fun Avatar(text: String) = Box(Modifier.size(42.dp).clip(CircleShape).background(AqyalGold), contentAlignment = Alignment.Center) { Text(text, color = Color.Black, fontWeight = FontWeight.Black) }

@Composable
private fun ChatHubScreen(
    account: AuthState.Authenticated,
    groups: GroupViewModel,
    directory: DirectoryViewModel,
    safety: SafetyViewModel,
    attachments: AttachmentViewModel,
    showGroups: Boolean
) {
    val tab = if (showGroups) 1 else 0
    var target by remember { mutableStateOf("") }
    var showDirectory by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<PublicRedProfile?>(null) }
    var directoryQuery by remember { mutableStateOf("") }
    var reportDetails by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var showEmoji by remember { mutableStateOf(false) }
    val decrypted = remember { mutableStateListOf<DecryptedMessage>() }
    val context = LocalContext.current
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null && target.isNotBlank()) attachments.send(uri, target, conversationId(account.redId, target))
    }
    var showSafetyScanner by remember { mutableStateOf(false) }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        showSafetyScanner = granted
        if (!granted) safety.cameraPermissionDenied()
    }
    LaunchedEffect(Unit) { DecryptedMessageBus.messages.collect { item ->
        decrypted.add(item)
        if (!item.outgoing) RedConnectionService.markRead(context, item.id, item.sequence)
    } }
    var create by remember { mutableStateOf(false) }
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var memberRedId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        if (tab == 0) Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (directory.requests.isNotEmpty()) {
                Text("طلبات الصداقة", color = AqyalGold, fontWeight = FontWeight.Bold)
                directory.requests.forEach { request ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AqyalSurfaceRaised)) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Avatar(request.requester.displayName.take(1)); Column(Modifier.weight(1f).padding(horizontal = 9.dp)) { Text(request.requester.displayName); Text("@${request.requester.username}", color = AqyalCyanGlow, fontSize = 11.sp) }
                            TextButton({ directory.resolve(request, false) }) { Text("رفض") }
                            Button({ directory.resolve(request, true) }) { Text("قبول") }
                        }
                    }
                }
            }
            if (directory.contacts.isNotEmpty()) {
                Text("الأصدقاء", color = AqyalGold, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(directory.contacts, key = { it.redId }) { person ->
                        Column(Modifier.widthIn(max = 86.dp).clickable { target = person.redId }, horizontalAlignment = Alignment.CenterHorizontally) {
                            Avatar(person.displayName.take(1)); Text(person.displayName, maxLines = 1, fontSize = 11.sp); Text("@${person.username}", color = AqyalCyanGlow, maxLines = 1, fontSize = 9.sp)
                            IconButton({ selectedContact = person }, Modifier.size(28.dp)) { Icon(Icons.Default.MoreVert, "إعدادات الصديق", Modifier.size(16.dp)) }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(target, { target = it.uppercase() }, Modifier.weight(1f), label = { Text("معرّف يونس للطرف الآخر") }, singleLine = true)
                IconButton({ showDirectory = true }) { Icon(Icons.Default.Contacts, "البحث عن أشخاص", tint = AqyalGold) }
            }
            val conversation = remember(account.redId, target) { conversationId(account.redId, target) }
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(decrypted.filter { it.conversationId == conversation }, key = { it.id }) { item ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (item.outgoing) Arrangement.End else Arrangement.Start) {
                        Card(
                            Modifier.widthIn(max = 320.dp),
                            colors = CardDefaults.cardColors(containerColor = if (item.outgoing) YounesEmerald.copy(alpha = .82f) else AqyalSurfaceRaised.copy(alpha = .94f)),
                            shape = RoundedCornerShape(
                                topStart = 20.dp, topEnd = 20.dp,
                                bottomStart = if (item.outgoing) 20.dp else 5.dp,
                                bottomEnd = if (item.outgoing) 5.dp else 20.dp
                            )
                        ) {
                            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Text(if (item.outgoing) "أنت" else item.senderRedId, color = if (item.outgoing) Color(0xB8002018) else AqyalCyanGlow, fontSize = 10.sp)
                                if (item.type == "FILE") AttachmentMessage(item, attachments)
                                else Text(item.plaintext.toString(Charsets.UTF_8), color = if (item.outgoing) Color(0xFF001B14) else Color.White, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
            if (showEmoji) LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                items(QUICK_EMOJI) { emoji -> TextButton({ messageText += emoji }) { Text(emoji, fontSize = 22.sp) } }
            }
            when (val attachmentState = attachments.state) {
                is AttachmentState.Working -> Text(attachmentState.message, color = AqyalGold, style = MaterialTheme.typography.bodySmall)
                is AttachmentState.Error -> Text("تعذر إكمال المرفق: ${attachmentState.message}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                is AttachmentState.Sent -> Text("تم إرسال ${attachmentState.name} مشفرًا", color = YounesEmerald, style = MaterialTheme.typography.bodySmall)
                is AttachmentState.Downloaded -> Text("تم التحقق وفك التشفير: ${attachmentState.name}", color = YounesEmerald, style = MaterialTheme.typography.bodySmall)
                AttachmentState.Idle -> Unit
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton({ showEmoji = !showEmoji }) { Icon(Icons.Default.EmojiEmotions, "الرموز التعبيرية") }
                IconButton({ filePicker.launch(arrayOf("image/*", "video/*", "audio/*", "application/pdf", "text/plain", "application/zip", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.openxmlformats-officedocument.presentationml.presentation")) }, enabled = target.matches(RED_ID_PATTERN) && attachments.state !is AttachmentState.Working) { Icon(Icons.Default.AttachFile, "ملف مشفر") }
                OutlinedTextField(messageText, { messageText = it }, Modifier.weight(1f), placeholder = { Text("رسالة مشفرة…") }, maxLines = 4)
                FilledIconButton({
                    RedConnectionService.sendText(context, target, conversation, messageText.trim()); messageText = ""; showEmoji = false
                }, enabled = target.matches(RED_ID_PATTERN) && messageText.isNotBlank()) { Icon(Icons.Default.Send, "إرسال") }
            }
        } else Column(Modifier.fillMaxSize().padding(14.dp)) {
            Button({ create = true }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Text(" إنشاء مجموعة") }
            when {
                groups.state == GroupState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally).padding(30.dp))
                groups.state is GroupState.Error -> EmptyState(Icons.Default.Groups, "تعذر تحميل المجموعات", (groups.state as GroupState.Error).message)
                groups.groups.isEmpty() -> EmptyState(Icons.Default.Groups, "لا توجد مجموعات", "أنشئ مجموعة محلية بأدوار مالك ومسؤول وعضو.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f).padding(top = 12.dp)) {
                    items(groups.groups, key = { it.id }) { group -> Card(Modifier.fillMaxWidth().clickable { selectedGroupId = group.id }) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Avatar(group.name.take(1)); Column(Modifier.padding(horizontal = 12.dp)) { Text(group.name, fontWeight = FontWeight.Bold); Text("${group.members.size} أعضاء · ${group.description.orEmpty()}", color = Color.Gray, fontSize = 12.sp) } } } }
                }
            }
        }
    }
    when (val safetyState = safety.state) {
        SafetyState.Closed -> Unit
        is SafetyState.Loading -> AlertDialog(onDismissRequest = safety::close, title = { Text("رمز الأمان") }, text = { Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AqyalGold) } }, confirmButton = { TextButton(safety::close) { Text("إلغاء") } })
        is SafetyState.Error -> AlertDialog(onDismissRequest = safety::close, title = { Text("تعذر التحقق") }, text = { Text(safetyState.message) }, confirmButton = { TextButton(safety::close) { Text("إغلاق") } })
        is SafetyState.Ready -> if (showSafetyScanner) AlertDialog(
            onDismissRequest = { showSafetyScanner = false },
            title = { Text("امسح رمز الطرف الآخر") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.fillMaxWidth().height(360.dp).clip(RoundedCornerShape(16.dp))) {
                        SafetyQrScanner(onCode = { safety.verifyScanned(it); showSafetyScanner = false })
                    }
                    Text("تتم المعالجة على الجهاز فقط، ولا تُرفع صور الكاميرا إلى الخادم.", fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            },
            confirmButton = { TextButton({ showSafetyScanner = false }) { Text("إلغاء") } }
        ) else AlertDialog(
            onDismissRequest = { safety.clearScanError(); safety.close() },
            title = { Text(if (safetyState.verified) "تم التحقق من الهوية" else "مقارنة رمز الأمان") },
            text = { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Image(safetyState.qr, "QR لرمز الأمان", Modifier.size(240.dp).clip(RoundedCornerShape(12.dp)))
                Text(safetyState.number, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = AqyalGold)
                Text("الجهاز ${safetyState.deviceId} · ${safetyState.fingerprint.chunked(8).joinToString(" ")}", fontSize = 9.sp, color = Color.Gray, textAlign = TextAlign.Center)
                safetyState.scanError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, textAlign = TextAlign.Center) }
                Text("امسح رمز الطرف الآخر وجهًا لوجه، أو قارن الرقم عبر قناة موثوقة مستقلة.", fontSize = 11.sp, textAlign = TextAlign.Center)
                if (!safetyState.verified) OutlinedButton({
                    safety.clearScanError()
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) showSafetyScanner = true
                    else cameraPermission.launch(Manifest.permission.CAMERA)
                }, Modifier.fillMaxWidth()) { Icon(Icons.Default.QrCodeScanner, null); Text(" مسح رمز الطرف الآخر") }
            } },
            confirmButton = { if (!safetyState.verified) Button(safety::markVerified) { Text("الأرقام متطابقة يدويًا") } else TextButton(safety::close) { Text("تم") } },
            dismissButton = { if (!safetyState.verified) TextButton(safety::close) { Text("إلغاء") } }
        )
    }
    selectedContact?.let { person ->
        AlertDialog(
            onDismissRequest = { selectedContact = null; reportDetails = "" },
            title = { Text(person.displayName) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("@${person.username}\n${person.redId}", color = AqyalCyanGlow)
                    OutlinedTextField(reportDetails, { reportDetails = it }, Modifier.fillMaxWidth(), label = { Text("تفاصيل بلاغ اختياري") }, maxLines = 4)
                    OutlinedButton({ safety.open(person.redId); selectedContact = null }, Modifier.fillMaxWidth()) { Text("رمز الأمان والتحقق") }
                    OutlinedButton({ directory.remove(person); selectedContact = null }, Modifier.fillMaxWidth()) { Text("إزالة من الأصدقاء") }
                    OutlinedButton({ directory.report(person, "SPAM", reportDetails); reportDetails = "" }, Modifier.fillMaxWidth()) { Text("إبلاغ عن إزعاج/احتيال") }
                    Button({ directory.block(person); selectedContact = null }, Modifier.fillMaxWidth()) { Text("حظر المستخدم") }
                }
            },
            confirmButton = { TextButton({ selectedContact = null; reportDetails = "" }) { Text("إغلاق") } }
        )
    }
    val selectedGroup = groups.groups.firstOrNull { it.id == selectedGroupId }
    if (selectedGroup != null) {
        val myRole = selectedGroup.members.firstOrNull { it.redId == account.redId }?.role
        val canManage = myRole == "OWNER" || myRole == "ADMIN"
        AlertDialog(
            onDismissRequest = { selectedGroupId = null },
            title = { Text(selectedGroup.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(selectedGroup.description.orEmpty(), color = Color.Gray)
                    LazyColumn(Modifier.height(220.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(selectedGroup.members, key = { it.id }) { member ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Avatar(member.username.take(1)); Column(Modifier.weight(1f).padding(horizontal = 10.dp)) { Text("@${member.username}"); Text(member.redId, color = AqyalCyanGlow, fontSize = 10.sp) }
                                AssistChip({}, { Text(member.role) }, enabled = false)
                            }
                        }
                    }
                    if (canManage) {
                        OutlinedTextField(memberRedId, { memberRedId = it.uppercase() }, Modifier.fillMaxWidth(), label = { Text("إضافة عضو بواسطة معرّف يونس") }, singleLine = true)
                        Button({ groups.addMember(selectedGroup, memberRedId) { memberRedId = "" } }, Modifier.fillMaxWidth(), enabled = memberRedId.matches(Regex("^(RED|YNS)-[23456789A-HJ-NP-Z]{4}-[23456789A-HJ-NP-Z]{4}$")) && groups.state != GroupState.Saving) { Text("إضافة عضو") }
                    }
                }
            },
            confirmButton = { TextButton({ selectedGroupId = null }) { Text("إغلاق") } },
            dismissButton = {
                if (myRole != "OWNER") TextButton({ groups.leave(selectedGroup) { selectedGroupId = null } }) { Text("مغادرة", color = MaterialTheme.colorScheme.error) }
            }
        )
    }
    if (showDirectory) AlertDialog(
        onDismissRequest = { showDirectory = false; directory.clear() },
        title = { Text("أشخاص يونس") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(directoryQuery, { directoryQuery = it }, Modifier.fillMaxWidth(), label = { Text("username أو معرّف يونس") }, singleLine = true)
                Button({ directory.search(directoryQuery) }, Modifier.fillMaxWidth(), enabled = directoryQuery.trim().length >= 3 && directory.state != DirectoryState.Loading) {
                    Icon(Icons.Default.Search, null); Text(" بحث آمن")
                }
                when (val state = directory.state) {
                    DirectoryState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally), color = AqyalGold)
                    is DirectoryState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                    is DirectoryState.Message -> Text(state.text, color = AqyalGold)
                    DirectoryState.Ready -> if (directory.results.isEmpty()) Text("لا توجد نتائج مطابقة", color = Color.Gray) else LazyColumn(Modifier.height(260.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        items(directory.results, key = { it.redId }) { person ->
                            Card(Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Avatar(person.displayName.take(1)); Column(Modifier.weight(1f).padding(start = 9.dp)) { Text(person.displayName, fontWeight = FontWeight.Bold); Text("@${person.username} · ${person.redId}", color = AqyalCyanGlow, fontSize = 10.sp) }
                                    TextButton({ target = person.redId; showDirectory = false; directory.clear() }) { Text("محادثة") }
                                    Button({ directory.request(person) }) { Text("إضافة") }
                                }
                            }
                        }
                    }
                    DirectoryState.Idle -> Text("ابحث عن شخص دون مشاركة رقم هاتف أو جهات اتصال الجهاز.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        },
        confirmButton = { TextButton({ showDirectory = false; directory.clear() }) { Text("إغلاق") } }
    )
    if (create) AlertDialog(onDismissRequest = { create = false }, title = { Text("مجموعة يونس جديدة") },
        text = { OutlinedTextField(name, { name = it }, label = { Text("اسم المجموعة") }, singleLine = true) },
        confirmButton = { Button({ groups.create(name, null) { create = false; name = "" } }, enabled = name.trim().length >= 2 && groups.state != GroupState.Saving) { Text("إنشاء") } },
        dismissButton = { OutlinedButton({ create = false }) { Text("إلغاء") } })
}

@Composable
private fun UnifiedCallsScreen(history: CallHistoryViewModel) {
    var filter by remember { mutableStateOf("الكل") }
    val visible = history.calls.filter { call -> when (filter) {
        "فائتة" -> call.status == "MISSED"; "صوت" -> call.type == "VOICE"; "فيديو" -> call.type == "VIDEO"
        "جماعية" -> call.type == "GROUP"; "بث" -> call.type == "LIVE"; "مساحات" -> call.type == "SPACE"
        "DINSTAR" -> call.route == "DINSTAR"; else -> true
    } }
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
        when {
            history.loading -> Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AqyalGold) }
            history.error != null -> EmptyState(Icons.Default.History, "تعذر تحميل السجل", history.error.orEmpty())
            visible.isEmpty() -> EmptyState(Icons.Default.History, "لا توجد مكالمات", "ستظهر هنا كل المكالمات مع شارة توضح مسار يونس أو DINSTAR.")
            else -> LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(visible, key = { it.id }) { CallHistoryRow(it) } }
        }
    }
}

@Composable
private fun CallHistoryRow(call: CallHistoryItem) = Card(Modifier.fillMaxWidth()) {
    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(if (call.route == "DINSTAR") AqyalGold else AqyalCyanGlow), contentAlignment = Alignment.Center) {
            Icon(if (call.type == "VIDEO") Icons.Default.Videocam else Icons.Default.Call, null, tint = Color.Black)
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(call.peerLabel.ifBlank { call.peerId }, fontWeight = FontWeight.Bold)
            Text("${if (call.direction == "OUTGOING") "صادرة" else "واردة"} · ${call.status}", color = if (call.status == "MISSED") Color.Red else Color.Gray, fontSize = 12.sp)
        }
        AssistChip({}, { Text(if (call.route == "DINSTAR") "DINSTAR صوت" else "يونس ${call.type}") }, enabled = false)
    }
}

@Composable
private fun RoundCallAction(icon: ImageVector, title: String, color: Color, enabled: Boolean) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    FilledIconButton({}, Modifier.size(62.dp), enabled = enabled) { Icon(icon, title, tint = if (enabled) color else Color.Gray, modifier = Modifier.size(30.dp)) }
    Text(title, fontSize = 11.sp); if (!enabled) Text("قيد الربط", color = Color.Gray, fontSize = 9.sp)
}

@Composable
private fun MoreScreen(account: AuthState.Authenticated, onDinstar: () -> Unit, onSettings: () -> Unit, onContacts: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("مساحة يونس", style = MaterialTheme.typography.headlineMedium)
        Text("الهوية والخدمات السيادية في مكان واحد", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(account.username.take(1))
                Column(Modifier.padding(horizontal = 12.dp)) {
                    Text(account.username, style = MaterialTheme.typography.titleMedium)
                    Text("@${account.username} · ${account.redId}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        MoreOption(Icons.Default.SimCard, "الهاتف اليمني", "اتصال صوتي مصرح عبر DINSTAR وشرائح الشبكات اليمنية", AqyalGold, click = onDinstar)
        MoreOption(Icons.Default.Settings, "الإعدادات والخصوصية", "الهوية والأجهزة والخادم والجلسة", YounesEmerald, click = onSettings)
        MoreOption(Icons.Default.Contacts, "جهات الاتصال", "الأصدقاء وطلبات التواصل والحظر", AqyalCyanGlow, click = onContacts)
        MoreOption(Icons.Default.Public, "المجتمعات والقنوات", "قيد التطوير — لن يُعرض كمكتمل قبل اختباره", Color(0xFFA78BFA), enabled = false) { }
    }
}

@Composable
private fun MoreOption(icon: ImageVector, title: String, detail: String, color: Color, enabled: Boolean = true, click: () -> Unit) =
    Card(Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = click)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color)
            }
            Column(Modifier.padding(horizontal = 14.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
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
private fun CreateSheet(publishing: Boolean, onDismiss: () -> Unit, onPost: (String) -> Unit, onStory: () -> Unit) {
    var composer by remember { mutableStateOf(false) }; var text by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("إنشاء في يونس", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            if (composer) {
                OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth().height(150.dp), placeholder = { Text("اكتب منشوراً، سلسلة، أو فكرة طويلة…") })
                Button({ if (text.isNotBlank()) onPost(text.trim()) }, Modifier.fillMaxWidth(), enabled = text.isNotBlank() && !publishing) { if (publishing) CircularProgressIndicator(Modifier.size(20.dp)) else Text("نشر محلي") }
            } else {
                CreateOption(Icons.Default.DynamicFeed, "منشور أو سلسلة", "نص طويل، اقتباس، استطلاع، صور وفيديو", true) { composer = true }
                CreateOption(Icons.Default.AddCircle, "حالة 24 ساعة", "صورة أو فيديو يُحذف تلقائياً", true, onStory)
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
        Text("الإعدادات", fontSize = 25.sp, fontWeight = FontWeight.Bold); FeatureCard("هوية يونس", "@${account.username}\n${account.redId}")
        FeatureCard("الخصوصية", "مفاتيح الهوية داخل Android Keystore · لا رقم هاتف · لا SMS")
        FeatureCard("الخادم", "Local-first عبر الشبكة الداخلية أو WireGuard")
        OutlinedButton(logout, Modifier.fillMaxWidth()) { Text("تسجيل الخروج") }; Spacer(Modifier.height(22.dp))
    }
}

@Composable private fun EmptyState(icon: ImageVector, title: String, detail: String) = Column(Modifier.fillMaxWidth().padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = AqyalGold, modifier = Modifier.size(62.dp)); Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(detail, textAlign = TextAlign.Center, color = Color.Gray, modifier = Modifier.padding(top = 8.dp)) }
@Composable private fun FeatureCard(title: String, detail: String) = Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text(title, color = AqyalGold, fontWeight = FontWeight.Bold); Text(detail) } }

@Composable
private fun AttachmentMessage(item: DecryptedMessage, attachments: AttachmentViewModel) {
    val manifestJson = item.plaintext.toString(Charsets.UTF_8)
    val manifest = remember(manifestJson) { runCatching { ATTACHMENT_JSON.decodeFromString<AttachmentManifest>(manifestJson) }.getOrNull() }
    if (manifest == null) {
        Text("مرفق مشفر غير صالح", color = MaterialTheme.colorScheme.error)
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.InsertDriveFile, null, modifier = Modifier.size(32.dp))
        Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text(manifest.name, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text("${manifest.mimeType} · ${formatBytes(manifest.size)}", style = MaterialTheme.typography.labelSmall)
        }
        IconButton({ attachments.download(manifestJson) }, enabled = attachments.state !is AttachmentState.Working) {
            Icon(Icons.Default.Download, "تنزيل وفك تشفير المرفق")
        }
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}

private val RED_ID_PATTERN = Regex("^(RED|YNS)-[23456789A-HJ-NP-Z]{4}-[23456789A-HJ-NP-Z]{4}$")
private val QUICK_EMOJI = listOf("😀", "😂", "😍", "👍", "❤️", "🔥", "👏", "🙏", "🎉", "😢", "😮", "✅")
private val ATTACHMENT_JSON = Json { ignoreUnknownKeys = true }

private fun conversationId(first: String, second: String): String {
    if (first.isBlank() || second.isBlank()) return "pending-conversation"
    val canonical = listOf(first, second).sorted().joinToString("|")
    return MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray()).joinToString("") { "%02x".format(it) }.take(32)
}
