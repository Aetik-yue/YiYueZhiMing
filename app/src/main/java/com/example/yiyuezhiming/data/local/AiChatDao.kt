package com.example.yiyuezhiming.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AiChatDao {
    @Query("SELECT * FROM ai_chat_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun observeMessages(sessionId: Long): Flow<List<AiChatMessageEntity>>

    @Query("SELECT * FROM ai_chat_messages WHERE sessionId = :sessionId AND status = 'sent' ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recentSentMessages(sessionId: Long, limit: Int): List<AiChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: AiChatMessageEntity): Long

    @Query("DELETE FROM ai_chat_messages WHERE sessionId = :sessionId")
    suspend fun clear(sessionId: Long)

    @Query("SELECT * FROM ai_assistants ORDER BY isPreset DESC, updatedAt DESC")
    fun observeAssistants(): Flow<List<AiAssistantEntity>>

    @Query("SELECT * FROM ai_assistants WHERE id = :id LIMIT 1")
    suspend fun getAssistant(id: Long): AiAssistantEntity?

    @Query("SELECT COUNT(*) FROM ai_assistants")
    suspend fun assistantCount(): Int

    @Query("SELECT * FROM ai_assistants ORDER BY isPreset DESC, updatedAt DESC LIMIT 1")
    suspend fun firstAssistant(): AiAssistantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAssistant(assistant: AiAssistantEntity): Long

    @Query("DELETE FROM ai_assistants WHERE id = :assistantId AND isPreset = 0")
    suspend fun deleteCustomAssistantRow(assistantId: Long)

    @Query("SELECT * FROM ai_chat_sessions WHERE assistantId = :assistantId ORDER BY updatedAt DESC")
    fun observeSessions(assistantId: Long): Flow<List<AiChatSessionEntity>>

    @Query("SELECT * FROM ai_chat_sessions WHERE assistantId = :assistantId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun latestSession(assistantId: Long): AiChatSessionEntity?

    @Query("SELECT * FROM ai_chat_sessions WHERE id = :id LIMIT 1")
    suspend fun getSession(id: Long): AiChatSessionEntity?

    @Query("SELECT COUNT(*) FROM ai_chat_sessions")
    suspend fun sessionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: AiChatSessionEntity): Long

    @Query("UPDATE ai_chat_sessions SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSessionTitle(id: Long, title: String, updatedAt: Long)

    @Query("UPDATE ai_chat_sessions SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun touchSession(id: Long, updatedAt: Long)

    @Query("DELETE FROM ai_chat_messages WHERE sessionId IN (SELECT id FROM ai_chat_sessions WHERE assistantId = :assistantId)")
    suspend fun deleteMessagesForAssistant(assistantId: Long)

    @Query("DELETE FROM ai_chat_sessions WHERE assistantId = :assistantId")
    suspend fun deleteSessionsForAssistant(assistantId: Long)

    @Query("DELETE FROM ai_chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Long)

    @Query("DELETE FROM ai_chat_sessions WHERE id = :sessionId")
    suspend fun deleteSessionRow(sessionId: Long)

    @Transaction
    suspend fun deleteCustomAssistant(assistantId: Long) {
        deleteMessagesForAssistant(assistantId)
        deleteSessionsForAssistant(assistantId)
        deleteCustomAssistantRow(assistantId)
    }

    @Transaction
    suspend fun deleteSession(sessionId: Long) {
        deleteMessagesForSession(sessionId)
        deleteSessionRow(sessionId)
    }
}
