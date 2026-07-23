package com.example.yiyuezhiming.ui.screens.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.StaggeredItem
import com.example.yiyuezhiming.ui.components.CloudChip
import com.example.yiyuezhiming.ui.components.EmptyStateView
import com.example.yiyuezhiming.ui.components.KawaiiSwitch
import com.example.yiyuezhiming.ui.components.KawaiiTextField
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.components.ReminderEnvelopeCard
import com.example.yiyuezhiming.ui.components.SleepingFoxCalendar
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.PeachGlow
import com.example.yiyuezhiming.ui.theme.SoftBlush
import com.example.yiyuezhiming.model.Reminder
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateReminderScreen(
    viewModel: DateReminderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    var actionReminder by remember { mutableStateOf<Reminder?>(null) }
    var deleteReminder by remember { mutableStateOf<Reminder?>(null) }
    AnimatedCloudBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KawaiiTopBar("重要的日子", showLogo = false) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        viewModel.startAdd()
                        showSheet = true
                    },
                    containerColor = AccentHotPink
                ) {
                    Text("♥", color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
            }
        ) { padding ->
            LazyColumn(
                Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { ReminderHero(nearest = state.reminders.firstOrNull()) }
                state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
                if (state.reminders.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "还没有重要的日子",
                            message = "点右下角加一个，开始一起倒数吧。",
                            animal = { SleepingFoxCalendar(Modifier.height(110.dp)) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    itemsIndexed(state.reminders, key = { _, item -> item.id }) { index, reminder ->
                        StaggeredItem(index) {
                            ReminderEnvelopeCard(
                                reminder = reminder,
                                isHighlighted = index == 0,
                                onClick = {
                                    viewModel.startEdit(reminder)
                                    showSheet = true
                                },
                                onLongClick = { actionReminder = reminder }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            AddReminderSheet(
                state = state,
                viewModel = viewModel,
                onSave = { viewModel.addReminder { showSheet = false } }
            )
        }
    }
    actionReminder?.let { reminder ->
        ReminderActionDialog(
            reminder = reminder,
            onDismiss = { actionReminder = null },
            onEdit = {
                viewModel.startEdit(reminder)
                actionReminder = null
                showSheet = true
            },
            onDelete = {
                actionReminder = null
                deleteReminder = reminder
            }
        )
    }
    deleteReminder?.let { reminder ->
        AlertDialog(
            onDismissRequest = { deleteReminder = null },
            title = { Text("删除这个日子？") },
            text = { Text("删除后会取消对应提醒，并从日期列表里移除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteReminder(reminder)
                        deleteReminder = null
                    }
                ) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteReminder = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun ReminderHero(nearest: Reminder?) {
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(28.dp), ambientColor = AccentHotPink.copy(alpha = 0.1f))
            .background(Brush.verticalGradient(listOf(PeachGlow, SoftBlush)), RoundedCornerShape(28.dp))
            .padding(horizontal = 24.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (nearest == null) {
            Text("还没有日子在倒数", color = AccentHotPink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("加一个重要的日期，开始期待吧", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
        } else {
            Text("下一个重要的日子", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            // 大倒计时数字
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (nearest.daysLeftValue <= 0L) {
                    Text("就是", color = AccentHotPink, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp)
                    Text("今天", color = AccentHotPink, fontWeight = FontWeight.ExtraBold, fontSize = 56.sp, modifier = Modifier.padding(bottom = 2.dp))
                } else {
                    Text(
                        "${nearest.daysLeftValue}",
                        color = AccentHotPink,
                        fontWeight = FontWeight.Black,
                        fontSize = 64.sp
                    )
                    Text(
                        "天",
                        color = AccentHotPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
            Text("后是「${nearest.title}」", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.titleMedium)
            val weekText = nearest.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINESE)
            Text(
                "${nearest.date.year}.${nearest.dateText.replace(" / ", ".")} · $weekText",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun AddReminderSheet(
    state: DateReminderUiState,
    viewModel: DateReminderViewModel,
    onSave: () -> Unit
) {
    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            if (state.editingReminder == null) "添加提醒" else "编辑提醒",
            style = MaterialTheme.typography.titleLarge,
            color = AccentHotPink,
            fontWeight = FontWeight.Bold
        )
        KawaiiTextField(state.title, viewModel::onTitleChanged, "标题")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("纪念日", "生日", "初见", "约会日", "自定义").forEach {
                CloudChip(it, selected = state.type == it, onClick = { viewModel.onTypeChanged(it) })
            }
        }
        KawaiiTextField(state.dateText, viewModel::onDateTextChanged, "日期 yyyy-MM-dd", error = state.error)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KawaiiSwitch(checked = state.enabled, onCheckedChange = viewModel::onEnabledChanged)
            Text("开启提醒，当天 09:00 通知")
        }
        CloudChip(if (state.editingReminder == null) "保存这个日子" else "保存修改", selected = true, onClick = onSave)
    }
}

@Composable
private fun ReminderActionDialog(
    reminder: Reminder,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(reminder.title) },
        text = { Text("想要编辑这个日期，还是删除它？") },
        confirmButton = { TextButton(onClick = onEdit) { Text("编辑") } },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDelete) { Text("删除", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}
