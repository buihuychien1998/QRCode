package com.example.qrcode.common.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.ByteArrayOutputStream




/**
 * Convert bitmap to byte array using ByteBuffer.
 */
fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

fun drawableToBitmap(drawable: Drawable): Bitmap? {
    var bitmap: Bitmap? = null
    if (drawable is BitmapDrawable) {
        val bitmapDrawable = drawable
        if (bitmapDrawable.bitmap != null) {
            return bitmapDrawable.bitmap
        }
    }
    bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
        Bitmap.createBitmap(
            1,
            1,
            Bitmap.Config.ARGB_8888
        ) // Single color bitmap will be created of 1x1 pixel
    } else {
        Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
    }
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun mergeBitmaps(logo: Bitmap?, qrcode: Bitmap): Bitmap? {
    val combined = Bitmap.createBitmap(qrcode.width, qrcode.height, qrcode.config)
    val canvas = Canvas(combined)
    val canvasWidth = canvas.width
    val canvasHeight = canvas.height
    canvas.drawBitmap(qrcode, Matrix(), null)
    val resizeLogo = Bitmap.createScaledBitmap(logo!!, canvasWidth / 5, canvasHeight / 5, true)
    val centreX = (canvasWidth - resizeLogo.width) / 2
    val centreY = (canvasHeight - resizeLogo.height) / 2
    canvas.drawBitmap(resizeLogo, centreX.toFloat(), centreY.toFloat(), null)
    return combined
}

fun String.encodeAsQrCodeBitmap(
    dimension: Int,
    overlayBitmap: Bitmap? = null,
    @ColorInt color1: Int = Color.BLUE,
    @ColorInt color2: Int = Color.WHITE
): Bitmap? {

    val result: BitMatrix
    try {
        result = MultiFormatWriter().encode(
            this,
            BarcodeFormat.QR_CODE,
            dimension,
            dimension,
            hashMapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H)
        )
    } catch (e: IllegalArgumentException) {
        // Unsupported format
        return null
    }

    val w = result.width
    val h = result.height
    val pixels = IntArray(w * h)
    for (y in 0 until h) {
        val offset = y * w
        for (x in 0 until w) {
            pixels[offset + x] = if (result.get(x, y)) color1 else color2
        }
    }
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, dimension, 0, 0, w, h)

    return if (overlayBitmap != null) {
        bitmap.addOverlayToCenter(overlayBitmap)
    } else {
        bitmap
    }
}

fun Bitmap.addOverlayToCenter(overlayBitmap: Bitmap): Bitmap {
    val bitmap2Width = overlayBitmap.width
    val bitmap2Height = overlayBitmap.height
    val marginLeft = (this.width * 0.5 - bitmap2Width * 0.5).toFloat()
    val marginTop = (this.height * 0.5 - bitmap2Height * 0.5).toFloat()
    val canvas = Canvas(this)
    canvas.drawBitmap(this, Matrix(), null)
    canvas.drawBitmap(overlayBitmap, marginLeft, marginTop, null)
    return this
}

fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}