package com.store.qrcode.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "history_table")
data class History(
    @ColumnInfo(name = "history") val qrCode: String?,
    @ColumnInfo(name = "create_date") val createDate: Long?
) {
    @PrimaryKey(autoGenerate = true)
    var historyId: Int = 0
}