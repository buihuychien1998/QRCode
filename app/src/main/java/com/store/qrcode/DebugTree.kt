package com.store.qrcode

import android.util.Log
import androidx.annotation.Nullable
import org.jetbrains.annotations.NotNull
import timber.log.Timber


open class DebugTree : Timber.DebugTree() {
    @Nullable
    override fun createStackElementTag(element: StackTraceElement): String? {
        return String.format(
            "C:%s:%s",
            super.createStackElementTag(element),
            element.lineNumber
        )
    }
}