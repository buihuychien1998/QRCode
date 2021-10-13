package com.example.qrcode.common.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.provider.Settings
import com.example.qrcode.R
import com.example.qrcode.common.LABEL_CLIPBOARD
import com.example.qrcode.presentation.service.RingtonePlayingService
import com.example.qrcode.presentation.service.RingtonePlayingService.Companion.KEY_RINGTONE_URI
import java.io.ByteArrayOutputStream
import java.util.*


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
//    val soundUri = Uri.parse(
//        "android.resource://"
//                + packageName + "/" + R.raw.custom_sound
//    )
    val uriString = "android.resource://" + packageName + "/" + R.raw.facebook_message_sound
//    val ring = RingtoneManager.getRingtone(this, soundUri)
//    ring.play()
    stopRingtone()
    startRingtone(uriString)
}

fun Context.startRingtone(ringtoneUri: String?) {
    val startIntent = Intent(this, RingtonePlayingService::class.java)
    startIntent.putExtra(KEY_RINGTONE_URI, ringtoneUri)
    startService(startIntent)
}

fun Context.stopRingtone() {
    val stopIntent = Intent(this, RingtonePlayingService::class.java)
    stopService(stopIntent)
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

fun Context.shareBitmap(b: Bitmap?, title: String) {
    val share = Intent(Intent.ACTION_SEND)
    share.type = "image/jpeg"
    val bytes = ByteArrayOutputStream()
    b?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(contentResolver, b, title, null)
    val imageUri = Uri.parse(path)
    share.putExtra(Intent.EXTRA_STREAM, imageUri)
    startActivity(Intent.createChooser(share, "Share"))
}

fun Context.changeLanguage(lang: String) {
    val config = resources.configuration
    val locale = Locale(lang)
    Locale.setDefault(locale)
    config.setLocale(locale)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
        createConfigurationContext(config)
//        triggerRebirth()
//        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
        return
    }
    resources.updateConfiguration(config, resources.displayMetrics)
//    triggerRebirth()
//    val intent = Intent(this, MainActivity::class.java)
//    startActivity(intent)
}

fun Context.triggerRebirth() {
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    val componentName = intent?.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)
    startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}

fun Activity.refreshLayout() {
    val intent = getIntent()
    overridePendingTransition(0, 0)
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
    finish()
    overridePendingTransition(0, 0)
    startActivity(intent)
}