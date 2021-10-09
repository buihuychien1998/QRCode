package com.example.qrcode.data.repository.local.qrcode

import androidx.annotation.WorkerThread
import com.example.qrcode.model.entity.Barcode
import kotlinx.coroutines.flow.Flow

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class QRCodeRepository(private val qrCodeDao: QRCodeDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val barList: Flow<List<Barcode>> = qrCodeDao.getQRList()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(barcode: Barcode): Long {
        return qrCodeDao.insert(barcode)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(barcode: Barcode): Int {
        return qrCodeDao.delele(barcode)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(barcode: Barcode): Int {
        return qrCodeDao.update(barcode)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(qrCode: String?, image: ByteArray?, genre: String, id: Int): Int {
        return qrCodeDao.update(qrCode, image, genre, id)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteById(qrCodeId: Long): Int {
        return qrCodeDao.deleteByUserId(qrCodeId)
    }

    suspend fun deleteAll(): Int {
        return qrCodeDao.deleteAll()
    }
}