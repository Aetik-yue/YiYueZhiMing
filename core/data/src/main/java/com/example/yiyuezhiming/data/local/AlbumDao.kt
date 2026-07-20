package com.example.yiyuezhiming.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Query("SELECT * FROM album_photos ORDER BY takenDate DESC, createdAt DESC")
    fun observePhotos(): Flow<List<AlbumPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: AlbumPhotoEntity): Long

    @Query("UPDATE album_photos SET memoryTag = :memoryTag, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTag(id: Long, memoryTag: String, updatedAt: Long)

    @Query("DELETE FROM album_photos WHERE id = :id")
    suspend fun deletePhoto(id: Long)

    @Query("SELECT * FROM album_categories ORDER BY createdAt ASC")
    fun observeCategories(): Flow<List<AlbumCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: AlbumCategoryEntity)

    @Query("SELECT COUNT(*) FROM album_categories")
    suspend fun categoryCount(): Int
}

