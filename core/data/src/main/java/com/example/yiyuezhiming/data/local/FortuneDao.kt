package com.example.yiyuezhiming.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface FortuneDao {
    @Query("SELECT * FROM fortune_records WHERE type = :type AND date = :date LIMIT 1")
    suspend fun getRecord(type: String, date: LocalDate): FortuneRecordEntity?

    @Query("SELECT * FROM fortune_records WHERE type = :type ORDER BY date DESC, createdAt DESC")
    fun observeHistory(type: String): Flow<List<FortuneRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: FortuneRecordEntity): Long
}

