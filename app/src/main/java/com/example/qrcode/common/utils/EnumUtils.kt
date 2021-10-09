package com.example.qrcode.common.utils

import androidx.annotation.IntDef
import androidx.annotation.StringDef
import com.example.qrcode.common.utils.QRGenre.Companion.PHONE
import com.example.qrcode.common.utils.QRGenre.Companion.TEXT
import com.example.qrcode.common.utils.QRGenre.Companion.WEBSITE

//Enumerated
@StringDef(TEXT, WEBSITE, PHONE)
@Retention(AnnotationRetention.SOURCE)
annotation class QRGenre{
    companion object{
        const val TEXT = "TEXT"
        const val WEBSITE = "WEBSITE"
        const val PHONE = "PHONE"
    }
}
