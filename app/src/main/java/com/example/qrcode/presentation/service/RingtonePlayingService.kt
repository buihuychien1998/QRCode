package com.example.qrcode.presentation.service

import android.app.Service
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder

class RingtonePlayingService : Service() {
    companion object{
        val KEY_RINGTONE_URI = "ringtone-uri"
    }
    private var ringtone: Ringtone? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        intent.extras?.let {bundle->
            bundle.getString(KEY_RINGTONE_URI)?.let {uri->
                val ringtoneUri = Uri.parse(uri)
                ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
                ringtone?.play()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        ringtone?.stop()
    }
}