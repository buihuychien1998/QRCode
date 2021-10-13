package com.store.qrcode.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.store.qrcode.presentation.ui.main.setting.SettingPreferences
import kotlinx.coroutines.flow.Flow

interface PrefsStore {
    fun getSetting(): Flow<SettingPreferences>

    suspend fun enableSetting(key: Preferences.Key<Boolean>, enable: Boolean)

    suspend fun changeLanguage(lang: String)

    fun getLanguage(): Flow<String>

    fun <T> DataStore<Preferences>.getValueFlow(
        key: Preferences.Key<T>,
        defaultValue: T
    ): Flow<T>

    suspend fun <T> DataStore<Preferences>.setValue(key: Preferences.Key<T>, value: T)
}