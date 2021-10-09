package com.example.qrcode

import androidx.annotation.Nullable
import timber.log.Timber

class NotLoggingTree : Timber.Tree() {
    protected override fun log(
        priority: Int, @Nullable tag: String?,
        message: String, @Nullable t: Throwable?
    ) {
        // Do nothing here
    }
}