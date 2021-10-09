package com.example.qrcode

import android.util.Log
import org.jetbrains.annotations.NotNull
import timber.log.Timber

class ReleaseTree : Timber.Tree() {
    override fun log(
        priority: Int, tag: String?,
        @NotNull message: String, t: Throwable?
    ) {
        if (priority == Log.ERROR || priority == Log.WARN) {
//            YourCrashLibrary.log(
//                priority,
//                tag,
//                message
//            )
        }
    }
}