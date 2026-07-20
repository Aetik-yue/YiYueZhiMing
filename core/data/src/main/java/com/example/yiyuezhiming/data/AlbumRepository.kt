package com.example.yiyuezhiming.data

import android.net.Uri
import com.example.yiyuezhiming.data.local.AlbumCategoryEntity
import com.example.yiyuezhiming.data.local.AlbumDao
import com.example.yiyuezhiming.model.AlbumPhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumRepository @Inject constructor(
    private val dao: AlbumDao,
    private val mediaStorage: MediaStorage
) {
    fun observePhotos(): Flow<List<AlbumPhoto>> =
        dao.observePhotos().map { rows -> rows.map { it.toModel() } }

    fun observeCategories(): Flow<List<String>> =
        dao.observeCategories().map { rows -> rows.map { it.name } }

    suspend fun importPhotos(category: String, sources: List<Uri>): Int {
        val trimmedCategory = category.trim()
        require(trimmedCategory.isNotEmpty()) { "请先选择相册分类" }
        ensureCategory(trimmedCategory)
        sources.forEach { source ->
            val privatePhoto = mediaStorage.copyToPrivateStorage(source, MediaStorage.Kind.Photo)
            val date = mediaStorage.readPhotoDate(source)
            dao.insertPhoto(
                AlbumPhoto(
                    category = trimmedCategory,
                    uri = privatePhoto,
                    takenDate = date
                ).toEntity()
            )
        }
        return sources.size
    }

    suspend fun updateTag(id: Long, memoryTag: String) =
        dao.updateTag(id, memoryTag.trim(), System.currentTimeMillis())

    suspend fun deletePhoto(id: Long) = dao.deletePhoto(id)

    suspend fun ensureDefaults() {
        if (dao.categoryCount() == 0) {
            listOf("甜蜜", "约会", "旅行", "日常", "纪念日").forEach { ensureCategory(it) }
        }
    }

    suspend fun ensureCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            dao.insertCategory(AlbumCategoryEntity(trimmed))
        }
    }
}

