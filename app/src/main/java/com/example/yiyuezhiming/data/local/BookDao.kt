package com.example.yiyuezhiming.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY COALESCE(lastReadAt, addedAt) DESC")
    fun observeBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    fun observeBook(bookId: String): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    suspend fun getBook(bookId: String): BookEntity?

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterIndex ASC")
    fun observeChapters(bookId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterIndex ASC")
    suspend fun getChapters(bookId: String): List<ChapterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChapters(chapters: List<ChapterEntity>)

    @Query("UPDATE books SET title = :title WHERE id = :bookId")
    suspend fun renameBook(bookId: String, title: String)

    @Query(
        """
        UPDATE books
        SET currentChapterIndex = :chapterIndex,
            currentPageInChapter = :pageIndex,
            lastReadAt = :lastReadAt
        WHERE id = :bookId
        """
    )
    suspend fun updateProgress(bookId: String, chapterIndex: Int, pageIndex: Int, lastReadAt: Long)

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChapters(bookId: String)

    @Query("DELETE FROM bookmarks WHERE bookId = :bookId")
    suspend fun deleteBookmarks(bookId: String)

    @Query("DELETE FROM reading_stats WHERE bookId = :bookId")
    suspend fun deleteStats(bookId: String)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookRow(bookId: String)

    @Transaction
    suspend fun deleteBook(bookId: String) {
        deleteChapters(bookId)
        deleteBookmarks(bookId)
        deleteStats(bookId)
        deleteBookRow(bookId)
    }
}
