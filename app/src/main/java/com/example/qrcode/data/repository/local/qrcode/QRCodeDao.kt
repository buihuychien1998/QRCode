package com.example.qrcode.data.repository.local.qrcode

import androidx.room.*
import com.example.qrcode.model.entity.Barcode
import kotlinx.coroutines.flow.Flow


@Dao
interface QRCodeDao {
    @Query("SELECT * FROM barcode_table ORDER BY create_date DESC")
    fun getQRList(): Flow<List<Barcode>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(barcode: Barcode): Long

    /**
     * Updating only price
     * By order id
     */
    @Query("UPDATE barcode_table SET qr_code=:qr_code, image=:image, genre=:genre WHERE qrCodeId = :id")
    suspend fun update(qr_code: String?, image: ByteArray?, genre: String, id: Int): Int

    @Update
    suspend fun update(order: Barcode?): Int

    @Delete
    suspend fun delele(barcode: Barcode): Int

    @Query("DELETE FROM barcode_table WHERE qrCodeId = :qrCodeId")
    suspend fun deleteByUserId(qrCodeId: Long): Int

    @Query("DELETE FROM history_table")
    suspend fun deleteAll(): Int
}