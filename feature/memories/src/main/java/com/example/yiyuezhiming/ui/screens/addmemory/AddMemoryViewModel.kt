package com.example.yiyuezhiming.ui.screens.addmemory

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yiyuezhiming.data.MemoryRepository
import com.example.yiyuezhiming.data.MockData
import com.example.yiyuezhiming.model.Memory
import com.example.yiyuezhiming.model.Mood
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddMemoryUiState(
    val selectedPhoto: Uri? = null,
    val selectedMusic: Uri? = null,
    val selectedMood: Mood = MockData.moods.first(),
    val songTitle: String = "",
    val artistName: String = "",
    val note: String = "",
    val date: LocalDate = LocalDate.now(),
    val category: String = "日常",
    val isSaving: Boolean = false,
    val showHearts: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class AddMemoryViewModel @Inject constructor(
    private val repository: MemoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddMemoryUiState())
    val uiState: StateFlow<AddMemoryUiState> = _uiState.asStateFlow()

    fun onPhotoSelected(uri: Uri?) = _uiState.update { it.copy(selectedPhoto = uri, error = null) }
    fun onMusicSelected(uri: Uri?) = _uiState.update { it.copy(selectedMusic = uri, error = null) }
    fun onMoodSelected(mood: Mood) = _uiState.update { it.copy(selectedMood = mood) }
    fun onSongTitleChanged(value: String) = _uiState.update { it.copy(songTitle = value) }
    fun onArtistNameChanged(value: String) = _uiState.update { it.copy(artistName = value) }
    fun onNoteChanged(value: String) = _uiState.update { it.copy(note = value, error = null) }
    fun onDateChanged(value: LocalDate) = _uiState.update { it.copy(date = value) }
    fun onCategoryChanged(value: String) = _uiState.update { it.copy(category = value) }
    fun consumeSuccess() = _uiState.update { it.copy(successMessage = null, showHearts = false) }

    fun save() {
        val state = _uiState.value
        if (state.note.isBlank()) {
            _uiState.update { it.copy(error = "想写下什么？这段记忆需要一点文字喔") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }
            try {
                repository.addMemory(
                    memory = Memory(
                        date = state.date,
                        mood = state.selectedMood,
                        note = state.note.trim(),
                        songTitle = state.songTitle.trim(),
                        artistName = state.artistName.trim(),
                        category = state.category
                    ),
                    sourcePhoto = state.selectedPhoto,
                    sourceMusic = state.selectedMusic
                )
                _uiState.value = AddMemoryUiState(
                    successMessage = "已经帮你收进回忆口袋啦",
                    showHearts = true
                )
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isSaving = false, error = error.message ?: "保存失败，请稍后重试")
                }
            }
        }
    }
}
