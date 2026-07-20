package com.example.yiyuezhiming.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    @Query("SELECT * FROM memos ORDER BY isPinned DESC, isDone ASC, updatedAt DESC")
    fun observeMemos(): Flow<List<MemoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(memo: MemoEntity): Long

    @Query("DELETE FROM memos WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE memos SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean, updatedAt: Long)

    @Query("UPDATE memos SET isDone = :isDone, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setDone(id: Long, isDone: Boolean, updatedAt: Long)

    @Query("SELECT * FROM memo_categories ORDER BY createdAt ASC")
    fun observeCategories(): Flow<List<MemoCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: MemoCategoryEntity)

    @Query("SELECT COUNT(*) FROM memo_categories")
    suspend fun categoryCount(): Int
}

