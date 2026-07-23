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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.example.yiyuezhiming.model.AlbumPhoto
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.StaggeredItem
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.components.BearWithAlbum
import com.example.yiyuezhiming.ui.components.CloudChip
import com.example.yiyuezhiming.ui.components.EmptyStateView
import com.example.yiyuezhiming.ui.components.HeartLoadingIndicator
import com.example.yiyuezhiming.ui.components.KawaiiTextField
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.components.CategoryInputDialog
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import com.example.yiyuezhiming.ui.theme.SecondaryPink
import com.example.yiyuezhiming.ui.theme.SkyBlush
import com.example.yiyuezhiming.ui.theme.SoftBlush
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

    val canImport = !state.isImporting && state.selectedCategory.isNotBlank()

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
                            selected = canImport,
                            onClick = {
                                if (canImport) {
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
                if (state.isImporting) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeartLoadingIndicator(Modifier.size(24.dp))
                        Text("导入中…", color = AccentHotPink, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Spacer(Modifier.height(10.dp))
                }
                // 分类筛选栏：clipToBounds 防止选中态 1.08x 缩放与邻居重叠
                LazyRow(
                    modifier = Modifier.clipToBounds(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.categories) { category ->
                        CloudChip(category, selected = state.selectedCategory == category, onClick = { viewModel.setCategory(category) })
                    }
                    item { CloudChip("+分类", selected = false, onClick = { showCategoryDialog = true }) }
                }
                Spacer(Modifier.height(12.dp))
                when {
                    state.isLoading -> Text("正在整理照片…", modifier = Modifier.padding(16.dp), color = AccentHotPink)
                    state.error != null -> Text(state.error.orEmpty(), modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                    state.visiblePhotos.isEmpty() -> EmptyStateView(
                        title = "这个分类还没有照片",
                        message = "选好当前分类后点导入，它们会乖乖留在这里。",
                        buttonText = "导入照片",
                        onButtonClick = {
                            if (canImport) {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        },
                        animal = { BearWithAlbum(Modifier.size(120.dp)) },
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> AlbumPhotoGrid(
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

/**
 * 3 列瀑布流网格：日期作为全宽分隔卡片，照片交错高度营造瀑布感。
 */
@Composable
private fun AlbumPhotoGrid(
    photos: List<AlbumPhoto>,
    onPhotoClick: (AlbumPhoto) -> Unit
) {
    val groups = photos.groupBy { it.takenDate }.toSortedMap(compareByDescending<LocalDate> { it })
    val flatItems = buildList {
        groups.forEach { (date, datePhotos) ->
            add(AlbumGridItem.DateHeader(date, datePhotos.size))
            datePhotos.forEachIndexed { index, photo ->
                add(AlbumGridItem.Photo(photo, index))
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 88.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        flatItems.forEachIndexed { gridIndex, item ->
            when (item) {
                is AlbumGridItem.DateHeader -> item(
                    key = "date-${item.date}",
                    span = { GridItemSpan(3) }
                ) {
                    StaggeredItem(gridIndex) {
                        DateHeaderRow(date = item.date, count = item.count)
                    }
                }
                is AlbumGridItem.Photo -> item(
                    key = "photo-${item.photo.id}",
                    span = { GridItemSpan(1) }
                ) {
                    StaggeredItem(gridIndex) {
                        AlbumPhotoTile(
                            photo = item.photo,
                            aspectRatio = masonryRatio(item.indexInDate),
                            onClick = { onPhotoClick(item.photo) }
                        )
                    }
                }
            }
        }
    }
}

private sealed interface AlbumGridItem {
    data class DateHeader(val date: LocalDate, val count: Int) : AlbumGridItem
    data class Photo(val photo: AlbumPhoto, val indexInDate: Int) : AlbumGridItem
}

/** 瀑布流交错高度：第 0/3 张偏高，第 1 张方，第 2 张略高，循环营造错落感 */
private fun masonryRatio(indexInDate: Int): Float = when (indexInDate % 3) {
    0 -> 0.78f   // 偏竖
    1 -> 1f      // 方
    else -> 0.88f // 略竖
}

@Composable
private fun DateHeaderRow(date: LocalDate, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 日期胶囊：相册专属 SkyBlush 渐变
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = PrimaryPink.copy(alpha = 0.18f))
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.horizontalGradient(listOf(SkyBlush, SoftBlush)))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "%02d.%02d".format(date.monthValue, date.dayOfMonth),
                    color = AccentHotPink,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                Text(
                    "${date.year}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        // 分隔线
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(PrimaryPink.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
        )
        Text(
            "$count 张",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AlbumPhotoTile(
    photo: AlbumPhoto,
    aspectRatio: Float,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .shadow(7.dp, RoundedCornerShape(18.dp), ambientColor = PrimaryPink.copy(alpha = 0.14f), spotColor = PrimaryPink.copy(alpha = 0.08f))
            .kawaiiClickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(SkyBlush, SecondaryPink.copy(alpha = 0.4f))))
        ) {
            SubcomposeAsyncImage(
                model = photo.uri,
                contentDescription = "相册照片",
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                        HeartLoadingIndicator(Modifier.size(22.dp))
                    }
                },
                error = {
                    Box(
                        Modifier.matchParentSize().background(CloudWhite.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("加载失败", color = AccentHotPink, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            )
            if (photo.memoryTag.isNotBlank()) {
                Text(
                    photo.memoryTag,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .background(Color.White.copy(alpha = 0.82f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
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
                // 响应式高度：最大 480dp，但允许根据屏幕收缩
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(CloudWhite.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = photo.uri,
                        contentDescription = "放大的相册照片",
                        modifier = Modifier.fillMaxWidth(),
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
