package com.example.yiyuezhiming.data

import com.example.yiyuezhiming.data.local.MemoCategoryEntity
import com.example.yiyuezhiming.data.local.MemoDao
import com.example.yiyuezhiming.model.Memo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoRepository @Inject constructor(
    private val dao: MemoDao
) {
    fun observeMemos(): Flow<List<Memo>> =
        dao.observeMemos().map { rows -> rows.map { it.toModel() } }

    fun observeCategories(): Flow<List<String>> =
        dao.observeCategories().map { rows -> rows.map { it.name } }

    suspend fun save(memo: Memo): Long {
        ensureCategory(memo.category)
        val now = System.currentTimeMillis()
        return dao.upsert(
            memo.copy(
                createdAt = if (memo.id == 0L) now else memo.createdAt,
                updatedAt = now
            ).toEntity()
        )
    }

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun setPinned(id: Long, value: Boolean) =
        dao.setPinned(id, value, System.currentTimeMillis())

    suspend fun setDone(id: Long, value: Boolean) =
        dao.setDone(id, value, System.currentTimeMillis())

    suspend fun ensureDefaults() {
        if (dao.categoryCount() == 0) {
            listOf("生活", "约会", "购物", "重要").forEach { ensureCategory(it) }
        }
    }

    suspend fun ensureCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            dao.insertCategory(MemoCategoryEntity(trimmed))
        }
    }
}

