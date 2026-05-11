package com.example.yiyuezhiming.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.yiyuezhiming.model.AiChatMessage
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
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
    AnimatedCloudBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                KawaiiTopBar(title = "AI", showLogo = true)
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DeepSeek 聊天", color = AccentHotPink, fontWeight = FontWeight.ExtraBold)
                    TextButton(onClick = { showClearDialog = true }) { Text("清空") }
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (state.messages.isEmpty()) {
                        item {
                            Text(
                                "在这里聊点想法、计划或者今天的小心情。使用前先到设置里保存 DeepSeek API Key。",
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
                                Text("正在回复…", color = AccentHotPink)
                            }
                        }
                    }
                }
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.input,
                        onValueChange = viewModel::setInput,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("输入想说的话") },
                        maxLines = 4
                    )
                    Button(
                        onClick = viewModel::send,
                        enabled = state.input.isNotBlank() && !state.isSending,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentHotPink)
                    ) {
                        Text("发送")
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空聊天记录？") },
            text = { Text("清空后本机保存的聊天历史会被删除。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clear()
                    showClearDialog = false
                }) { Text("清空") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("取消") } }
        )
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
            modifier = Modifier.fillMaxWidth(0.82f),
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
                    .padding(14.dp)
            ) {
                Text(
                    message.content,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

