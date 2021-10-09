package com.example.qrcode.data.repository.local.history

import androidx.room.*
import com.example.qrcode.model.entity.History
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_table ORDER BY create_date DESC")
    fun getHistory(): Flow<List<History>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(history: History): Long

    @Delete
    suspend fun delele(history: History): Int

    @Query("DELETE FROM history_table")
    suspend fun deleteAll(): Int
}