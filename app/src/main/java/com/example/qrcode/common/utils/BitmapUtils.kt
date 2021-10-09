package com.example.qrcode.common.utils

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

/**
 * Convert bitmap to byte array using ByteBuffer.
 */
fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}