package com.store.qrcode.common.utils

import androidx.annotation.StringDef
import com.store.qrcode.common.utils.Languages.Companion.DEFAULT
import com.store.qrcode.common.utils.Languages.Companion.VIETNAMESE
import com.store.qrcode.common.utils.QRGenre.Companion.PHONE
import com.store.qrcode.common.utils.QRGenre.Companion.TEXT
import com.store.qrcode.common.utils.QRGenre.Companion.WEBSITE

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
@StringDef(DEFAULT, VIETNAMESE)
@Retention(AnnotationRetention.SOURCE)
annotation class Languages{
    companion object{
        const val DEFAULT = "en"
        const val VIETNAMESE = "vi"
    }
}
