package com.example.yiyuezhiming.ui.screens.novel

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.yiyuezhiming.model.Book
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import com.example.yiyuezhiming.ui.theme.SoftBlush

@Composable
fun NovelBookshelfScreen(
    onOpenReader: (String) -> Unit,
    viewModel: NovelBookshelfViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var renameBook by remember { mutableStateOf<Book?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val name = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && index >= 0) cursor.getString(index) else null
        }
        viewModel.import(uri, name)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    AnimatedCloudBackground {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                KawaiiTopBar(title = "小说", showLogo = true)
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::setQuery,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("搜索书名或作者") },
                        singleLine = true,
                        shape = RoundedCornerShape(22.dp)
                    )
                    Button(
                        onClick = { picker.launch(arrayOf("application/epub+zip", "application/octet-stream", "*/*")) },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentHotPink)
                    ) { Text("导入") }
                }
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SortChip("最近阅读", state.sort == BookshelfSort.LAST_READ) { viewModel.setSort(BookshelfSort.LAST_READ) }
                    SortChip("最近添加", state.sort == BookshelfSort.ADDED) { viewModel.setSort(BookshelfSort.ADDED) }
                    SortChip("书名", state.sort == BookshelfSort.TITLE) { viewModel.setSort(BookshelfSort.TITLE) }
                }
                if (state.books.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
                        Text("导入一本 EPUB，把想读的故事放进自己的小书架。", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(state.books, key = { it.id }) { book ->
                            BookCard(
                                book = book,
                                onClick = { if (book.status == "READY") onOpenReader(book.id) },
                                onLongClick = { selectedBook = book }
                            )
                        }
                        item { Spacer(Modifier.height(88.dp)) }
                    }
                }
            }
            SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
        }
    }

    selectedBook?.let { book ->
        AlertDialog(
            onDismissRequest = { selectedBook = null },
            title = { Text(book.title) },
            text = { Text("作者：${book.author}\n章节：${book.totalChapters}\n状态：${book.status}") },
            confirmButton = { TextButton(onClick = { renameBook = book; selectedBook = null }) { Text("重命名") } },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.delete(book); selectedBook = null }) { Text("删除") }
                    TextButton(onClick = { selectedBook = null }) { Text("取消") }
                }
            }
        )
    }

    renameBook?.let { book ->
        var title by rememberSaveable(book.id) { mutableStateOf(book.title) }
        AlertDialog(
            onDismissRequest = { renameBook = null },
            title = { Text("重命名") },
            text = {
                OutlinedTextField(value = title, onValueChange = { title = it.take(40) }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = { viewModel.rename(book, title); renameBook = null }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { renameBook = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun SortChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) Brush.horizontalGradient(listOf(PrimaryPink, AccentHotPink))
                else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.9f), CloudWhite.copy(alpha = 0.78f))),
                RoundedCornerShape(999.dp)
            )
            .kawaiiClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = if (selected) Color.White else AccentHotPink, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookCard(book: Book, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.86f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .background(Brush.linearGradient(listOf(SoftBlush, CloudWhite, PrimaryPink.copy(alpha = 0.3f))), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverPath != null) {
                    AsyncImage(model = java.io.File(book.coverPath), contentDescription = book.title, modifier = Modifier.fillMaxSize())
                } else {
                    Text("小说", color = AccentHotPink, fontWeight = FontWeight.ExtraBold)
                }
            }
            Text(book.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.ExtraBold, color = AccentHotPink)
            Text(book.author, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            if (book.status == "IMPORTING") {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AccentHotPink)
                Text("导入中", color = AccentHotPink, style = MaterialTheme.typography.bodySmall)
            } else if (book.status == "FAILED") {
                Text(book.errorMessage ?: "导入失败", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            } else {
                LinearProgressIndicator(progress = { book.progress }, modifier = Modifier.fillMaxWidth(), color = AccentHotPink)
                Text("${(book.progress * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
