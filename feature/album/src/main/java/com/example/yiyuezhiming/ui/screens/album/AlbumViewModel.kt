package com.example.yiyuezhiming.ui.screens.album

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yiyuezhiming.data.AlbumRepository
import com.example.yiyuezhiming.model.AlbumPhoto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumUiState(
    val selectedCategory: String = "",
    val categories: List<String> = emptyList(),
    val photos: List<AlbumPhoto> = emptyList(),
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
) {
    val visiblePhotos: List<AlbumPhoto>
        get() = photos.filter { it.category == selectedCategory }
}

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val repository: AlbumRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaults()
            combine(repository.observeCategories(), repository.observePhotos()) { categories, photos ->
                categories to photos
            }.catch { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "相册加载失败") }
            }.collect { (categories, photos) ->
                _uiState.update { current ->
                    val selected = when {
                        current.selectedCategory in categories -> current.selectedCategory
                        categories.isNotEmpty() -> categories.first()
                        else -> ""
                    }
                    current.copy(
                        selectedCategory = selected,
                        categories = categories,
                        photos = photos,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setCategory(value: String) = _uiState.update { it.copy(selectedCategory = value) }

    fun addCategory(value: String) {
        val name = value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            repository.ensureCategory(name)
            _uiState.update { it.copy(selectedCategory = name) }
        }
    }

    fun importPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val category = _uiState.value.selectedCategory
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, error = null, successMessage = null) }
            try {
                val count = repository.importPhotos(category, uris)
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        successMessage = "已导入 $count 张照片到「$category」"
                    )
                }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        error = error.message ?: "导入照片失败，请稍后重试"
                    )
                }
            }
        }
    }

    fun updateTag(photo: AlbumPhoto, value: String) {
        viewModelScope.launch {
            repository.updateTag(photo.id, value)
        }
    }

    fun deletePhoto(photo: AlbumPhoto) {
        viewModelScope.launch {
            repository.deletePhoto(photo.id)
        }
    }

    fun consumeSuccess() = _uiState.update { it.copy(successMessage = null) }
}

