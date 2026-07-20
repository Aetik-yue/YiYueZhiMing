package com.example.yiyuezhiming.ui.screens.novel

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.yiyuezhiming.model.Book
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.LavenderMist
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import com.example.yiyuezhiming.ui.theme.SoftBlush
import java.io.File

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
    var showOnlineFetch by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val name = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && index >= 0) cursor.getString(index) else null
        }
        viewModel.import(uri, name, context.contentResolver.getType(uri))
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

                // Search bar + import buttons
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::setQuery,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("🔍 搜索书名或作者", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White.copy(alpha = 0.92f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.92f)
                        )
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { picker.launch(arrayOf("application/epub+zip", "text/plain", "application/octet-stream", "*/*")) },
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentHotPink),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text("＋ 导入", fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { showOnlineFetch = true }) {
                            Text("在线抓取", fontSize = 12.sp, color = AccentHotPink.copy(alpha = 0.8f))
                        }
                    }
                }

                // Sort chips
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SortChip("最近阅读", state.sort == BookshelfSort.LAST_READ) { viewModel.setSort(BookshelfSort.LAST_READ) }
                    SortChip("最近添加", state.sort == BookshelfSort.ADDED) { viewModel.setSort(BookshelfSort.ADDED) }
                    SortChip("书名", state.sort == BookshelfSort.TITLE) { viewModel.setSort(BookshelfSort.TITLE) }
                }

                // Content: empty state or book grid
                if (state.books.isEmpty()) {
                    EmptyBookshelf(
                        onImport = { picker.launch(arrayOf("application/epub+zip", "text/plain", "application/octet-stream", "*/*")) }
                    )
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

    // Long-press book info dialog
    selectedBook?.let { book ->
        AlertDialog(
            onDismissRequest = { selectedBook = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White.copy(alpha = 0.96f),
            title = {
                Text(
                    book.title,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentHotPink,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DialogInfoRow(label = "作者", value = book.author)
                    DialogInfoRow(label = "章节", value = "${book.totalChapters} 章")
                    DialogInfoRow(label = "状态", value = when (book.status) {
                        "READY" -> "可读"
                        "IMPORTING" -> "导入中"
                        "FAILED" -> "导入失败"
                        else -> book.status
                    })
                    if (book.status == "FAILED" && book.errorMessage != null) {
                        Text(
                            book.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { renameBook = book; selectedBook = null }) {
                    Text("重命名", color = AccentHotPink, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.delete(book); selectedBook = null }) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { selectedBook = null }) {
                        Text("取消")
                    }
                }
            }
        )
    }

    // Rename dialog
    renameBook?.let { book ->
        var title by rememberSaveable(book.id) { mutableStateOf(book.title) }
        AlertDialog(
            onDismissRequest = { renameBook = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White.copy(alpha = 0.96f),
            title = { Text("重命名", fontWeight = FontWeight.Bold, color = AccentHotPink) },
            text = {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(40) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPink,
                        unfocusedBorderColor = PrimaryPink.copy(alpha = 0.4f)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.rename(book, title); renameBook = null }) {
                    Text("保存", color = AccentHotPink, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameBook = null }) {
                    Text("取消")
                }
            }
        )
    }

    // Online fetch dialog
    if (showOnlineFetch) {
        var url by rememberSaveable(showOnlineFetch) { mutableStateOf(state.defaultSourceUrl) }
        var title by rememberSaveable(showOnlineFetch) { mutableStateOf("") }
        var saveAsDefault by rememberSaveable(showOnlineFetch) { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showOnlineFetch = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White.copy(alpha = 0.96f),
            title = { Text("在线抓取", fontWeight = FontWeight.Bold, color = AccentHotPink) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "输入目录页或正文页链接，抓取结果会以 TXT 加入书架。默认源只保存在本机。",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it.take(500) },
                        label = { Text("网页地址") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPink,
                            unfocusedBorderColor = PrimaryPink.copy(alpha = 0.4f)
                        )
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(40) },
                        label = { Text("书名（可选）") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPink,
                            unfocusedBorderColor = PrimaryPink.copy(alpha = 0.4f)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("保存为默认源", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Switch(checked = saveAsDefault, onCheckedChange = { saveAsDefault = it })
                    }
                    if (state.defaultSourceUrl.isNotBlank()) {
                        TextButton(onClick = {
                            viewModel.clearDefaultSourceUrl()
                            url = ""
                            saveAsDefault = false
                        }) {
                            Text("清除已保存的默认源", color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (saveAsDefault) viewModel.saveDefaultSourceUrl(url.trim())
                    viewModel.fetchOnline(url.trim(), title.trim().ifBlank { null })
                    showOnlineFetch = false
                }) {
                    Text("开始", color = AccentHotPink, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOnlineFetch = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun EmptyBookshelf(onImport: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("📚", fontSize = 64.sp)
            Text(
                "小书架还是空的",
                color = AccentHotPink,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "导入 EPUB 或 TXT，把想读的故事放进来吧",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onImport,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentHotPink),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
            ) {
                Text("导入书籍", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
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

@Composable
private fun DialogInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            value,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodyMedium
        )
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
        colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPink.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Cover area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(SoftBlush, LavenderMist.copy(alpha = 0.6f), PrimaryPink.copy(alpha = 0.25f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverPath != null) {
                    AsyncImage(
                        model = File(book.coverPath),
                        contentDescription = book.title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Decorative first character + book emoji
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            if (book.title.isNotEmpty()) book.title[0].toString() else "书",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentHotPink.copy(alpha = 0.7f)
                        )
                        Text("📖", fontSize = 20.sp)
                    }
                }

                // Chapter count chip in top-end corner
                if (book.totalChapters > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(
                                Color.White.copy(alpha = 0.85f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "${book.totalChapters}章",
                            fontSize = 10.sp,
                            color = AccentHotPink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Title
            Text(
                book.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.ExtraBold,
                color = AccentHotPink,
                style = MaterialTheme.typography.bodyMedium
            )

            // Author
            Text(
                book.author,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall
            )

            // Progress / status
            if (book.status == "IMPORTING") {
                KawaiiProgressBar(progress = null)
                Text("导入中…", color = AccentHotPink, style = MaterialTheme.typography.bodySmall)
            } else if (book.status == "FAILED") {
                Text(
                    book.errorMessage ?: "导入失败",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                KawaiiProgressBar(progress = book.progress)
                Text(
                    "${(book.progress * 100).toInt()}%",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun KawaiiProgressBar(progress: Float?) {
    val trackColor = SoftBlush.copy(alpha = 0.5f)
    val fillColor = Brush.horizontalGradient(listOf(PrimaryPink, AccentHotPink))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(trackColor)
    ) {
        if (progress != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(fillColor)
            )
        } else {
            // Indeterminate: show a pulsing partial fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(fillColor)
            )
        }
    }
}
