package com.example.qrcode.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.qrcode.data.repository.local.history.HistoryDao
import com.example.qrcode.data.repository.local.qrcode.QRCodeDao
import com.example.qrcode.model.entity.History
import com.example.qrcode.model.entity.Barcode


// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(entities = [History::class, Barcode::class], version = 1, exportSchema = true)
abstract class QRCodeDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao

    abstract fun qrCodeDao(): QRCodeDao

    companion object {
//        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                // Since we didn't alter the table, there's nothing else to do here.
//            }
//        }

        private val QR_CODE_DATABASE = "qr_code_database"

        // Singleton prevents multiple instances of database opening at the
        // same time. 
        @Volatile
        private var INSTANCE: QRCodeDatabase? = null

        fun getDatabase(context: Context): QRCodeDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QRCodeDatabase::class.java,
                    QR_CODE_DATABASE
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}