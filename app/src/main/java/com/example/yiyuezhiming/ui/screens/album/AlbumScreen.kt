package com.example.yiyuezhiming.ui.screens.album

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.example.yiyuezhiming.model.AlbumPhoto
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.components.CloudChip
import com.example.yiyuezhiming.ui.components.EmptyStateView
import com.example.yiyuezhiming.ui.components.HeartLoadingIndicator
import com.example.yiyuezhiming.ui.components.KawaiiTextField
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.screens.memo.CategoryInputDialog
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.BackgroundPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import com.example.yiyuezhiming.ui.theme.SecondaryPink
import java.time.LocalDate

@Composable
fun AlbumScreen(
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedPhoto by remember { mutableStateOf<AlbumPhoto?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris: List<Uri> ->
        viewModel.importPhotos(uris)
    }

    LaunchedEffect(state.successMessage) {
        val message = state.successMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeSuccess()
    }

    AnimatedCloudBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                KawaiiTopBar(
                    title = "我们的相册",
                    showLogo = false,
                    right = {
                        CloudChip(
                            text = if (state.isImporting) "导入中" else "导入",
                            selected = true,
                            onClick = {
                                if (!state.isImporting && state.selectedCategory.isNotBlank()) {
                                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            }
                        )
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "每个分类都有自己的照片，不再混在一起",
                        color = AccentHotPink,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.isImporting) HeartLoadingIndicator(Modifier.size(24.dp))
                }
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.categories) { category ->
                        CloudChip(category, selected = state.selectedCategory == category, onClick = { viewModel.setCategory(category) })
                    }
                    item { CloudChip("+分类", selected = false, onClick = { showCategoryDialog = true }) }
                }
                Spacer(Modifier.height(14.dp))
                when {
                    state.isLoading -> Text("正在整理照片…", modifier = Modifier.padding(16.dp), color = AccentHotPink)
                    state.error != null -> Text(state.error.orEmpty(), modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                    state.visiblePhotos.isEmpty() -> EmptyStateView(
                        title = "这个分类还没有照片",
                        message = "选择当前分类后导入照片，它们只会留在这个分类里。",
                        buttonText = "导入照片",
                        onButtonClick = {
                            if (state.selectedCategory.isNotBlank()) {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        },
                        animal = { Box(Modifier.size(1.dp)) },
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> AlbumTimeline(
                        photos = state.visiblePhotos,
                        onPhotoClick = { selectedPhoto = it }
                    )
                }
            }
        }
    }

    selectedPhoto?.let { photo ->
        AlbumPhotoPreview(
            photo = photo,
            onDismiss = { selectedPhoto = null },
            onSaveTag = { viewModel.updateTag(photo, it) },
            onDelete = {
                viewModel.deletePhoto(photo)
                selectedPhoto = null
            }
        )
    }

    if (showCategoryDialog) {
        CategoryInputDialog(
            title = "新增相册分类",
            onDismiss = { showCategoryDialog = false },
            onConfirm = {
                viewModel.addCategory(it)
                showCategoryDialog = false
            }
        )
    }
}

@Composable
private fun AlbumTimeline(
    photos: List<AlbumPhoto>,
    onPhotoClick: (AlbumPhoto) -> Unit
) {
    val groups = photos.groupBy { it.takenDate }.toSortedMap(compareByDescending<LocalDate> { it })
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 16.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        groups.forEach { (date, datePhotos) ->
            item(key = date.toString()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimelineDate(date = date, modifier = Modifier.width(68.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        datePhotos.chunked(2).forEach { rowPhotos ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                rowPhotos.forEach { photo ->
                                    AlbumPhotoTile(
                                        photo = photo,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onPhotoClick(photo) }
                                    )
                                }
                                if (rowPhotos.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineDate(date: LocalDate, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "%02d.%02d".format(date.monthValue, date.dayOfMonth),
            color = AccentHotPink,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "${date.year}",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .width(3.dp)
                .height(96.dp)
                .background(PrimaryPink.copy(alpha = 0.42f), RoundedCornerShape(999.dp))
        )
    }
}

@Composable
private fun AlbumPhotoTile(
    photo: AlbumPhoto,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(7.dp, RoundedCornerShape(18.dp), ambientColor = PrimaryPink.copy(alpha = 0.12f))
            .kawaiiClickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(CloudWhite, SecondaryPink.copy(alpha = 0.42f))))
        ) {
            SubcomposeAsyncImage(
                model = photo.uri,
                contentDescription = "相册照片",
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop,
                error = {
                    Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                        Text("加载失败", color = AccentHotPink, fontWeight = FontWeight.Bold)
                    }
                }
            )
            if (photo.memoryTag.isNotBlank()) {
                Text(
                    photo.memoryTag,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.78f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = AccentHotPink,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AlbumPhotoPreview(
    photo: AlbumPhoto,
    onDismiss: () -> Unit,
    onSaveTag: (String) -> Unit,
    onDelete: () -> Unit
) {
    var tag by remember(photo.id, photo.memoryTag) { mutableStateOf(photo.memoryTag) }
    var confirmDelete by remember { mutableStateOf(false) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable(onClick = onDismiss)
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {}),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(CloudWhite.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = photo.uri,
                        contentDescription = "放大的相册照片",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Text(photo.dateText, color = Color.White.copy(alpha = 0.72f), fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(24.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        KawaiiTextField(tag, { tag = it }, "这张照片的小标签")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = { confirmDelete = true },
                                modifier = Modifier.weight(1f)
                            ) { Text("删除照片", color = MaterialTheme.colorScheme.error) }
                            TextButton(
                                onClick = {
                                    onSaveTag(tag)
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .background(AccentHotPink, RoundedCornerShape(999.dp))
                            ) { Text("保存标签", color = Color.White, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
            Text(
                text = "×",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 8.dp)
                    .size(48.dp)
                    .kawaiiClickable(onClick = onDismiss)
                    .padding(top = 2.dp)
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除这张照片？") },
            text = { Text("删除后它会从当前相册分类中移除。") },
            confirmButton = {
                TextButton(onClick = onDelete) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("取消") } }
        )
    }
}

