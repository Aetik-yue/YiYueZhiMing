package com.example.yiyuezhiming.ui.screens.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.StaggeredItem
import com.example.yiyuezhiming.ui.components.CloudChip
import com.example.yiyuezhiming.ui.components.KawaiiSwitch
import com.example.yiyuezhiming.ui.components.KawaiiTextField
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.components.ReminderEnvelopeCard
import com.example.yiyuezhiming.ui.components.SleepingFoxCalendar
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateReminderScreen(
    viewModel: DateReminderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    AnimatedCloudBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KawaiiTopBar("重要的日子", showLogo = false) },
            floatingActionButton = {
                FloatingActionButton(onClick = { showSheet = true }, containerColor = AccentHotPink) {
                    Text("♥", color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
            }
        ) { padding ->
            LazyColumn(
                Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { ReminderHero() }
                state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
                itemsIndexed(state.reminders, key = { _, item -> item.id }) { index, reminder ->
                    StaggeredItem(index) { ReminderEnvelopeCard(reminder) }
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
}

@Composable
private fun ReminderHero() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Color(0xFFFFD6A5), PrimaryPink.copy(alpha = 0.72f), CloudWhite)), RoundedCornerShape(28.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text("日期提醒", color = AccentHotPink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("把重要的日子轻轻放进心里", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        SleepingFoxCalendar(Modifier.weight(0.65f))
    }
}

@Composable
private fun AddReminderSheet(
    state: DateReminderUiState,
    viewModel: DateReminderViewModel,
    onSave: () -> Unit
) {
    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("添加提醒", style = MaterialTheme.typography.titleLarge, color = AccentHotPink, fontWeight = FontWeight.Bold)
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
        CloudChip("保存这个日子", selected = true, onClick = onSave)
    }
}
