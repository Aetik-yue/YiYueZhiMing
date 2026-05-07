package com.example.yiyuezhiming.ui.screens.album

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.yiyuezhiming.data.MockData
import com.example.yiyuezhiming.model.Memory
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.animation.StaggeredItem
import com.example.yiyuezhiming.ui.components.AlbumPhotoCard
import com.example.yiyuezhiming.ui.components.CatWithAlbum
import com.example.yiyuezhiming.ui.components.CloudChip
import com.example.yiyuezhiming.ui.components.EmptyStateView
import com.example.yiyuezhiming.ui.components.HeartLoadingIndicator
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.components.PawIcon
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.CloudWhite
import coil.compose.AsyncImage

@Composable
fun AlbumScreen(
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedMemory by remember { mutableStateOf<Memory?>(null) }
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
                                if (!state.isImporting) {
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "把所有喜欢的瞬间拼成一座小小相册",
                        color = AccentHotPink,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.isImporting) HeartLoadingIndicator(Modifier.size(24.dp))
                    else PawIcon(Modifier.size(24.dp))
                }
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(MockData.albumFilters.size) { index ->
                        val item = MockData.albumFilters[index]
                        CloudChip(item, selected = state.filter == item, onClick = { viewModel.setFilter(item) })
                    }
                }
                Spacer(Modifier.height(14.dp))
                Crossfade(state.filter, label = "album-filter") {
                    when {
                        state.isLoading -> Text("正在整理照片…", modifier = Modifier.padding(16.dp), color = AccentHotPink)
                        state.error != null -> Text(state.error.orEmpty(), modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                        state.filtered.isEmpty() -> EmptyStateView(
                            title = "还没有照片哦",
                            message = "去添加记忆并选择照片后，它们会安全复制到应用本地相册。",
                            animal = { CatWithAlbum() },
                            modifier = Modifier.fillMaxSize()
                        )
                        else -> LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.filtered, key = { it.id }) { memory ->
                                StaggeredItem(memory.id.toInt()) {
                                    AlbumPhotoCard(memory, onClick = { selectedMemory = memory })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedMemory?.let { memory ->
        AlbumPhotoPreview(
            memory = memory,
            onDismiss = { selectedMemory = null }
        )
    }
}

@Composable
private fun AlbumPhotoPreview(
    memory: Memory,
    onDismiss: () -> Unit
) {
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
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(CloudWhite.copy(alpha = 0.1f))
                        .clickable(onClick = {})
                ) {
                    AsyncImage(
                        model = memory.photoUri,
                        contentDescription = "放大的相册照片",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(520.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(Modifier.height(18.dp))
                Box(
                    modifier = Modifier
                        .background(CloudWhite.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "${memory.mood.label} · ${memory.dateText}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
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
}
