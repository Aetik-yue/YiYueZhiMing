package com.example.yiyuezhiming.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AiChatDao {
    @Query("SELECT * FROM ai_chat_messages ORDER BY createdAt ASC")
    fun observeMessages(): Flow<List<AiChatMessageEntity>>

    @Query("SELECT * FROM ai_chat_messages WHERE status = 'sent' ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recentSentMessages(limit: Int): List<AiChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: AiChatMessageEntity): Long

    @Query("DELETE FROM ai_chat_messages")
    suspend fun clear()
}

