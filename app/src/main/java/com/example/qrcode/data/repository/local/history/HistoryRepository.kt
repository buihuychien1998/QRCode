package com.example.qrcode.data.repository.local.history

import androidx.annotation.WorkerThread
import com.example.qrcode.model.entity.History
import kotlinx.coroutines.flow.Flow

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class HistoryRepository(private val historyDao: HistoryDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allHistories: Flow<List<History>> = historyDao.getHistory()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(history: History): Long {
        return historyDao.insert(history)
    }

    suspend fun delete(history: History) = historyDao.delele(history)

    suspend fun deleteAll(): Int {
        return historyDao.deleteAll()
    }
}