package com.example.yiyuezhiming.ui.screens.addmemory

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.yiyuezhiming.data.MockData
import com.example.yiyuezhiming.ui.animation.AnimatedCloudBackground
import com.example.yiyuezhiming.ui.animation.FloatingHearts
import com.example.yiyuezhiming.ui.animation.kawaiiClickable
import com.example.yiyuezhiming.ui.components.CloudChip
import com.example.yiyuezhiming.ui.components.HeartLoadingIndicator
import com.example.yiyuezhiming.ui.components.KawaiiTextField
import com.example.yiyuezhiming.ui.components.KawaiiTopBar
import com.example.yiyuezhiming.ui.components.MoodSelector
import com.example.yiyuezhiming.ui.components.MusicCatIcon
import com.example.yiyuezhiming.ui.components.PolaroidPhotoPicker
import com.example.yiyuezhiming.ui.theme.AccentHotPink
import com.example.yiyuezhiming.ui.theme.PrimaryPink
import java.time.LocalDate

@Composable
fun AddMemoryScreen(
    onBack: () -> Unit,
    viewModel: AddMemoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        viewModel.onPhotoSelected(uri)
    }
    val musicPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        viewModel.onMusicSelected(uri)
    }

    LaunchedEffect(state.successMessage) {
        val message = state.successMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeSuccess()
    }

    AnimatedCloudBackground {
        if (state.showHearts) FloatingHearts(Modifier.fillMaxSize(), 14)
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KawaiiTopBar("记录这一刻", showBack = true, onBack = onBack) },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("把今天的心动悄悄收藏", color = PrimaryPink, style = MaterialTheme.typography.bodyLarge)
                Card(
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                    modifier = Modifier.shadow(16.dp, RoundedCornerShape(30.dp), ambientColor = PrimaryPink.copy(alpha = 0.16f), spotColor = AccentHotPink.copy(alpha = 0.14f))
                ) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        PolaroidPhotoPicker(
                            selected = state.selectedPhoto != null,
                            onClick = {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        KawaiiTextField(
                            value = state.date.toString(),
                            onValueChange = { runCatching { LocalDate.parse(it) }.onSuccess(viewModel::onDateChanged) },
                            label = "日期 yyyy-MM-dd"
                        )
                        MoodSelector(MockData.moods, state.selectedMood, viewModel::onMoodSelected)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MockData.albumFilters.take(6).forEach { category ->
                                CloudChip(
                                    text = category,
                                    selected = state.category == category,
                                    onClick = { viewModel.onCategoryChanged(category) }
                                )
                            }
                        }
                        KawaiiTextField(state.songTitle, viewModel::onSongTitleChanged, "歌曲名", leadingIcon = { MusicCatIcon() })
                        KawaiiTextField(state.artistName, viewModel::onArtistNameChanged, "艺术家")
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .kawaiiClickable { musicPicker.launch(arrayOf("audio/*")) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MusicCatIcon()
                            Text(if (state.selectedMusic == null) "导入本地音乐" else "已选择音乐，保存时会复制到本地")
                        }
                        KawaiiTextField(state.note, viewModel::onNoteChanged, "写下只属于我们的甜蜜…", minLines = 5, error = state.error)
                        Button(
                            onClick = viewModel::save,
                            enabled = !state.isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentHotPink),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            AnimatedContent(state.isSaving, label = "save") { isSaving ->
                                if (isSaving) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    HeartLoadingIndicator()
                                    Text("正在收藏…")
                                } else {
                                    Text("存入时光胶囊", color = Color.White, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
