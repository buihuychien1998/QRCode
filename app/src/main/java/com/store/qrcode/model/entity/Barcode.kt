package com.store.qrcode.model.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.store.qrcode.common.utils.QRGenre
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "barcode_table")
data class Barcode(
    @ColumnInfo(name = "qr_code") val qrCode: String?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val image: ByteArray?,
    @QRGenre @ColumnInfo(name = "genre") val genre: String,
    @ColumnInfo(name = "create_date") val createDate: Long?
): Parcelable {
    @PrimaryKey(autoGenerate = true)
    var qrCodeId: Int = 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Barcode) return false

        if (qrCodeId != other.qrCodeId) return false

        return true
    }

    override fun hashCode(): Int {
        return qrCodeId
    }
}