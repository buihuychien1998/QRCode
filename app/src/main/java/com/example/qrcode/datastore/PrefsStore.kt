package com.example.qrcode.datastore

import androidx.datastore.preferences.core.Preferences
import com.example.qrcode.presentation.ui.main.setting.SettingPreferences
import kotlinx.coroutines.flow.Flow

interface PrefsStore {
    fun getSetting(): Flow<SettingPreferences>

    suspend fun enableSetting(key: Preferences.Key<Boolean>, enable: Boolean)
}