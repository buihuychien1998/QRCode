package com.example.qrcode.common.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.qrcode.R
import com.example.qrcode.common.LABEL_CLIPBOARD
import java.io.ByteArrayOutputStream


fun Context.vibrate() {
    val vibrate = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
// Vibrate for 500 milliseconds
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrate.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        //deprecated in API 26
        vibrate.vibrate(500)
    }
}

fun Context.ring() {
    val notification =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val soundUri = Uri.parse(
        "android.resource://"
                + packageName + "/" + R.raw.custom_sound
    )
//    val ring = RingtoneManager.getRingtone(this, notification)
    val ring = RingtoneManager.getRingtone(this, soundUri)
    ring.play()
}

fun Context.copy(value: String?) {
    val clipboard: ClipboardManager =
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(LABEL_CLIPBOARD, value)
    clipboard.setPrimaryClip(clip)
}

fun Context.openSetting() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivity(intent)
}

// Create and return the Share Intent
fun Context.createShareIntent(text: String?) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(
        Intent.EXTRA_TEXT,
        text
    )
    val intent = Intent.createChooser(shareIntent, "Share")
    startActivity(intent)
}

fun Context.shareBitmap(b: Bitmap?){
    val share = Intent(Intent.ACTION_SEND)
    share.type = "image/jpeg"
    val bytes = ByteArrayOutputStream()
    b?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(contentResolver, b, "Title", null)
    val imageUri = Uri.parse(path)
    share.putExtra(Intent.EXTRA_STREAM, imageUri)
    startActivity(Intent.createChooser(share, "Share"))
}