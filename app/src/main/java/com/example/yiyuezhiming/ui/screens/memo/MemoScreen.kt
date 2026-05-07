package com.example.yiyuezhiming.ui.screens.memo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.yiyuezhiming.model.Memo
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.components.CloudChip
import com.example.yiyuezhiming.ui.components.EmptyStateView
import com.example.yiyuezhiming.ui.components.KawaiiTextField
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.BackgroundPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import com.example.yiyuezhiming.ui.theme.SecondaryPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoScreen(
    viewModel: MemoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var editingMemo by remember { mutableStateOf<Memo?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deletingMemo by remember { mutableStateOf<Memo?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    AnimatedCloudBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KawaiiTopBar(title = "备忘录", showLogo = true) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingMemo = null
                        showEditor = true
                    },
                    shape = CircleShape,
                    containerColor = AccentHotPink,
                    contentColor = Color.White
                ) {
                    Text("+", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                KawaiiTextField(
                    value = state.search,
                    onValueChange = viewModel::setSearch,
                    label = "搜索备忘录"
                )
                Spacer(Modifier.height(12.dp))
                MemoCategoryRow(
                    categories = state.categories,
                    selected = state.selectedCategory,
                    onSelect = viewModel::setCategory,
                    onAdd = { showCategoryDialog = true }
                )
                Spacer(Modifier.height(12.dp))
                when {
                    state.isLoading -> Text("正在整理备忘录…", color = AccentHotPink)
                    state.error != null -> Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    state.filtered.isEmpty() -> EmptyStateView(
                        title = "还没有备忘",
                        message = "写一条简单的小提醒，重要的事情就不会溜走。",
                        animal = { Box(Modifier.size(1.dp)) },
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 92.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.filtered, key = { it.id }) { memo ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                MemoCard(
                                    memo = memo,
                                    onClick = {
                                        editingMemo = memo
                                        showEditor = true
                                    },
                                    onToggleDone = { viewModel.toggleDone(memo) },
                                    onTogglePinned = { viewModel.togglePinned(memo) },
                                    onDelete = { deletingMemo = memo }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        AddEditMemoBottomSheet(
            memo = editingMemo,
            categories = state.categories,
            defaultCategory = if (state.selectedCategory == "全部") state.categories.firstOrNull().orEmpty() else state.selectedCategory,
            onDismiss = { showEditor = false },
            onSave = { memo ->
                viewModel.saveMemo(memo)
                showEditor = false
            }
        )
    }

    deletingMemo?.let { memo ->
        AlertDialog(
            onDismissRequest = { deletingMemo = null },
            title = { Text("删除备忘录？") },
            text = { Text("删除后这条备忘就不会出现在列表里了。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMemo(memo)
                        deletingMemo = null
                    }
                ) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deletingMemo = null }) { Text("取消") } }
        )
    }

    if (showCategoryDialog) {
        CategoryInputDialog(
            title = "新增备忘分类",
            onDismiss = { showCategoryDialog = false },
            onConfirm = {
                viewModel.addCategory(it)
                showCategoryDialog = false
            }
        )
    }
}

@Composable
private fun MemoCategoryRow(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item { CloudChip("全部", selected == "全部", { onSelect("全部") }) }
        items(categories) { category ->
            CloudChip(category, selected == category, { onSelect(category) })
        }
        item { CloudChip("+分类", false, onAdd) }
    }
}

@Composable
private fun MemoCard(
    memo: Memo,
    onClick: () -> Unit,
    onToggleDone: () -> Unit,
    onTogglePinned: () -> Unit,
    onDelete: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (memo.isDone) CloudWhite.copy(alpha = 0.78f) else Color.White.copy(alpha = 0.94f),
        label = "memo-card-color"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = PrimaryPink.copy(alpha = 0.12f))
            .kawaiiClickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(background)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier
                        .background(if (memo.isPinned) AccentHotPink else SecondaryPink, RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(if (memo.isPinned) "置顶" else memo.category, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text(
                    memo.title,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentHotPink,
                    textDecoration = if (memo.isDone) TextDecoration.LineThrough else TextDecoration.None
                )
            }
            if (memo.content.isNotBlank()) {
                Text(
                    memo.content,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (memo.isDone) 0.45f else 0.72f),
                    textDecoration = if (memo.isDone) TextDecoration.LineThrough else TextDecoration.None
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                MemoAction(if (memo.isDone) "已完成" else "完成", onToggleDone)
                MemoAction(if (memo.isPinned) "取消置顶" else "置顶", onTogglePinned)
                Spacer(Modifier.weight(1f))
                MemoAction("删除", onDelete, danger = true)
            }
        }
    }
}

@Composable
private fun MemoAction(text: String, onClick: () -> Unit, danger: Boolean = false) {
    Text(
        text = text,
        color = if (danger) MaterialTheme.colorScheme.error else AccentHotPink,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(BackgroundPink.copy(alpha = 0.72f), RoundedCornerShape(999.dp))
            .kawaiiClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMemoBottomSheet(
    memo: Memo?,
    categories: List<String>,
    defaultCategory: String,
    onDismiss: () -> Unit,
    onSave: (Memo) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember(memo) { mutableStateOf(memo?.title.orEmpty()) }
    var content by remember(memo) { mutableStateOf(memo?.content.orEmpty()) }
    var category by remember(memo, defaultCategory) { mutableStateOf(memo?.category ?: defaultCategory.ifBlank { categories.firstOrNull().orEmpty() }) }
    var customCategory by remember { mutableStateOf("") }
    val canSave = title.trim().isNotEmpty() && (category.isNotBlank() || customCategory.trim().isNotEmpty())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                if (memo == null) "新增备忘录" else "编辑备忘录",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = AccentHotPink
            )
            KawaiiTextField(title, { title = it }, "标题")
            KawaiiTextField(content, { content = it }, "内容", minLines = 4)
            Text("分类", color = AccentHotPink, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categories) { item ->
                    CloudChip(item, category == item && customCategory.isBlank(), {
                        category = item
                        customCategory = ""
                    })
                }
            }
            KawaiiTextField(
                value = customCategory,
                onValueChange = {
                    customCategory = it
                    if (it.isNotBlank()) category = it
                },
                label = "或输入新分类"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("取消") }
                TextButton(
                    enabled = canSave,
                    onClick = {
                        val now = System.currentTimeMillis()
                        onSave(
                            Memo(
                                id = memo?.id ?: 0,
                                title = title.trim(),
                                content = content.trim(),
                                category = customCategory.trim().ifBlank { category.trim() },
                                isPinned = memo?.isPinned ?: false,
                                isDone = memo?.isDone ?: false,
                                createdAt = memo?.createdAt ?: now,
                                updatedAt = now
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            Brush.horizontalGradient(listOf(PrimaryPink, AccentHotPink)),
                            RoundedCornerShape(999.dp)
                        )
                ) { Text("保存", color = Color.White, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
fun CategoryInputDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("分类名称") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                enabled = value.trim().isNotEmpty(),
                onClick = { onConfirm(value.trim()) }
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
