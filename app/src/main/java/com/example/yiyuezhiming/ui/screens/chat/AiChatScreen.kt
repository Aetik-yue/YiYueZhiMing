package com.example.yiyuezhiming.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.yiyuezhiming.model.AiAssistant
import com.example.yiyuezhiming.model.AiChatMessage
import com.example.yiyuezhiming.model.AiChatSession
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.components.MarkdownText
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import com.example.yiyuezhiming.ui.theme.SoftBlush

@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var showAssistantManager by remember { mutableStateOf(false) }
    var editingAssistant by remember { mutableStateOf<AiAssistant?>(null) }
    var creatingAssistant by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<AiChatSession?>(null) }
    var assistantToDelete by remember { mutableStateOf<AiAssistant?>(null) }

    AnimatedCloudBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KawaiiTopBar(title = "小助手", showLogo = true) },
            bottomBar = {
                ChatInputBar(
                    value = state.input,
                    isSending = state.isSending,
                    onValueChange = viewModel::setInput,
                    onSend = viewModel::send
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChatHeader(
                    assistant = state.activeAssistant,
                    onManageAssistants = { showAssistantManager = true },
                    onConfig = { editingAssistant = state.activeAssistant },
                    onNewSession = viewModel::createSession,
                    onClear = { showClearDialog = true }
                )
                SessionRow(
                    sessions = state.sessions,
                    activeSessionId = state.activeSessionId,
                    onSelect = viewModel::switchSession,
                    onDelete = { sessionToDelete = it }
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (state.messages.isEmpty()) {
                        item {
                            Text(
                                "开始一个新的对话吧。使用前请先到设置里保存 DeepSeek API Key，也可以为不同小助手设置名字、图标和人格。",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                            )
                        }
                    }
                    items(state.messages, key = { it.id }) { message ->
                        ChatBubble(message)
                    }
                    if (state.isSending) {
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = AccentHotPink)
                                Text("${state.assistantName} 正在回复…", color = AccentHotPink)
                            }
                        }
                    }
                }
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    if (showAssistantManager) {
        AssistantManagerDialog(
            assistants = state.assistants,
            activeAssistantId = state.activeAssistantId,
            onDismiss = { showAssistantManager = false },
            onSelect = {
                viewModel.switchAssistant(it.id)
                showAssistantManager = false
            },
            onAdd = {
                creatingAssistant = true
                showAssistantManager = false
            },
            onEdit = {
                editingAssistant = it
                showAssistantManager = false
            },
            onDelete = { assistantToDelete = it }
        )
    }

    if (creatingAssistant || editingAssistant != null) {
        AssistantEditDialog(
            assistant = editingAssistant,
            onDismiss = {
                creatingAssistant = false
                editingAssistant = null
            },
            onSave = { assistant, name, icon, prompt, webSearch ->
                viewModel.saveAssistantConfig(assistant, name, icon, prompt, webSearch)
                creatingAssistant = false
                editingAssistant = null
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空当前会话？") },
            text = { Text("清空后，当前会话里的消息会从本机删除，其他历史会话不受影响。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clear()
                    showClearDialog = false
                }) { Text("清空") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("取消") } }
        )
    }

    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("删除会话？") },
            text = { Text("「${session.title.ifBlank { "新的对话" }}」里的消息会一并删除。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSession(session.id)
                    sessionToDelete = null
                }) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { sessionToDelete = null }) { Text("取消") } }
        )
    }

    assistantToDelete?.let { assistant ->
        AlertDialog(
            onDismissRequest = { assistantToDelete = null },
            title = { Text("删除小助手？") },
            text = { Text("删除「${assistant.name}」后，它的所有会话也会从本机删除。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAssistant(assistant)
                    assistantToDelete = null
                }) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { assistantToDelete = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun ChatHeader(
    assistant: AiAssistant?,
    onManageAssistants: () -> Unit,
    onConfig: () -> Unit,
    onNewSession: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .kawaiiClickable(onClick = onManageAssistants),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                assistant?.icon ?: "💗",
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.82f), CircleShape)
                    .padding(9.dp),
                style = MaterialTheme.typography.titleMedium
            )
            Column {
                Text(
                    assistant?.name ?: "小睿睿",
                    color = AccentHotPink,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    if (assistant?.webSearchEnabled == true) "已开启联网搜索" else "点击切换或管理小助手",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.48f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onNewSession) { Text("新会话") }
            TextButton(onClick = onConfig) { Text("配置") }
            TextButton(onClick = onClear) { Text("清空") }
        }
    }
}

@Composable
private fun SessionRow(
    sessions: List<AiChatSession>,
    activeSessionId: Long,
    onSelect: (Long) -> Unit,
    onDelete: (AiChatSession) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sessions, key = { it.id }) { session ->
            val selected = session.id == activeSessionId
            Row(
                modifier = Modifier
                    .background(
                        if (selected) Brush.horizontalGradient(listOf(PrimaryPink, AccentHotPink))
                        else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.9f), CloudWhite.copy(alpha = 0.82f))),
                        RoundedCornerShape(999.dp)
                    )
                    .kawaiiClickable(onClick = { onSelect(session.id) })
                    .padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    session.title.ifBlank { "新的对话" }.take(16),
                    color = if (selected) Color.White else AccentHotPink,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "×",
                    modifier = Modifier
                        .background(Color.White.copy(alpha = if (selected) 0.2f else 0.68f), CircleShape)
                        .kawaiiClickable(onClick = { onDelete(session) })
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                    color = if (selected) Color.White else AccentHotPink,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: AiChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 304.dp),
            shape = RoundedCornerShape(
                topStart = 22.dp,
                topEnd = 22.dp,
                bottomStart = if (isUser) 22.dp else 6.dp,
                bottomEnd = if (isUser) 6.dp else 22.dp
            ),
            colors = CardDefaults.cardColors(Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (isUser) Brush.horizontalGradient(listOf(PrimaryPink, AccentHotPink))
                        else Brush.horizontalGradient(listOf(Color.White, SoftBlush, CloudWhite))
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                val normalized = message.content.replace(Regex("(?<!以)越之名"), "以越之名")
                if (isUser) {
                    Text(
                        normalized,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    MarkdownText(
                        markdown = normalized,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    isSending: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .background(Color.White.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(26.dp),
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentHotPink,
                unfocusedBorderColor = PrimaryPink.copy(alpha = 0.48f),
                focusedContainerColor = Color.White.copy(alpha = 0.78f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.62f)
            )
        )
        Button(
            onClick = onSend,
            enabled = value.isNotBlank() && !isSending,
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentHotPink)
        ) {
            Text("发送")
        }
    }
}

@Composable
private fun AssistantManagerDialog(
    assistants: List<AiAssistant>,
    activeAssistantId: Long,
    onDismiss: () -> Unit,
    onSelect: (AiAssistant) -> Unit,
    onAdd: () -> Unit,
    onEdit: (AiAssistant) -> Unit,
    onDelete: (AiAssistant) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("小助手") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CloudWhite.copy(alpha = 0.9f), RoundedCornerShape(999.dp))
                        .kawaiiClickable(onClick = onAdd)
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Text("+ 添加助手", color = AccentHotPink, fontWeight = FontWeight.Bold)
                }
                LazyColumn(
                    modifier = Modifier.height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(assistants, key = { it.id }) { assistant ->
                        val selected = assistant.id == activeAssistantId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (selected) SoftBlush.copy(alpha = 0.95f) else Color.Transparent,
                                    RoundedCornerShape(18.dp)
                                )
                                .kawaiiClickable(onClick = { onSelect(assistant) })
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                assistant.icon,
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                                    .padding(7.dp)
                            )
                            Column(Modifier.weight(1f)) {
                                Text(assistant.name, fontWeight = FontWeight.Bold)
                                Text(
                                    if (assistant.webSearchEnabled) "联网搜索 · ${if (assistant.isPreset) "预置" else "自定义"}" else if (assistant.isPreset) "预置助手" else "自定义助手",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            TextButton(onClick = { onEdit(assistant) }) { Text("编辑") }
                            if (!assistant.isPreset) {
                                TextButton(onClick = { onDelete(assistant) }) { Text("删") }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("完成") } }
    )
}

@Composable
private fun AssistantEditDialog(
    assistant: AiAssistant?,
    onDismiss: () -> Unit,
    onSave: (AiAssistant?, String, String, String, Boolean) -> Unit
) {
    var name by rememberSaveable(assistant?.id) { mutableStateOf(assistant?.name ?: "") }
    var icon by rememberSaveable(assistant?.id) { mutableStateOf(assistant?.icon ?: "💗") }
    var prompt by rememberSaveable(assistant?.id) { mutableStateOf(assistant?.prompt ?: "") }
    var webSearch by rememberSaveable(assistant?.id) { mutableStateOf(assistant?.webSearchEnabled ?: false) }
    val icons = listOf("💗", "😊", "🌐", "📊", "📱", "🗄️", "🧰", "🎮", "💻", "✨")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (assistant == null) "添加助手" else if (assistant.isPreset) "复制并调整助手" else "编辑助手") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(icons) { candidate ->
                        Text(
                            candidate,
                            modifier = Modifier
                                .background(if (candidate == icon) SoftBlush else CloudWhite.copy(alpha = 0.9f), CircleShape)
                                .border(1.dp, if (candidate == icon) AccentHotPink else Color.Transparent, CircleShape)
                                .kawaiiClickable(onClick = { icon = candidate })
                                .padding(9.dp)
                        )
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(24) },
                    label = { Text("名称") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it.take(4_000) },
                    label = { Text("人格提示词") },
                    minLines = 7,
                    maxLines = 10
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("联网搜索", fontWeight = FontWeight.Bold)
                        Text("回答前先搜索网页结果", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f), style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = webSearch, onCheckedChange = { webSearch = it })
                }
                if (assistant?.isPreset == true) {
                    Text(
                        "保存后会创建一个自定义副本，预置助手本身不会被覆盖。",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(assistant, name, icon, prompt, webSearch) }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
