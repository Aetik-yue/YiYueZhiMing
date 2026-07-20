package com.example.yiyuezhiming.data

import android.net.Uri
import com.example.yiyuezhiming.data.local.MemoryDao
import com.example.yiyuezhiming.model.Memory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepository @Inject constructor(
    private val dao: MemoryDao,
    private val mediaStorage: MediaStorage
) {
    fun observeMemories(): Flow<List<Memory>> =
        dao.observeMemories().map { rows -> rows.map { it.toModel() } }

    fun observePhotoMemories(): Flow<List<Memory>> =
        dao.observePhotoMemories().map { rows -> rows.map { it.toModel() } }

    suspend fun addMemory(memory: Memory, sourcePhoto: Uri?, sourceMusic: Uri?): Long {
        val privatePhoto = sourcePhoto?.let { mediaStorage.copyToPrivateStorage(it, MediaStorage.Kind.Photo) }
        val privateMusic = sourceMusic?.let { mediaStorage.copyToPrivateStorage(it, MediaStorage.Kind.Music) }
        return dao.insert(
            memory.copy(
                photoUri = privatePhoto ?: memory.photoUri,
                musicUri = privateMusic ?: memory.musicUri
            ).toEntity()
        )
    }

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun seedIfEmpty(memories: List<Memory>) {
        if (dao.count() == 0) {
            memories.forEach { dao.insert(it.toEntity()) }
        }
    }
}
