package com.example.qrcode.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.qrcode.common.utils.Languages
import com.example.qrcode.datastore.PrefsStore
import com.example.qrcode.presentation.ui.main.setting.SettingPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private const val SETTING_PREFERENCES_NAME = "user_preferences"

//val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = DATA_STORE_NAME,
    produceMigrations = { context ->
        // Since we're migrating from SharedPreferences, add a migration based on the
        // SharedPreferences name
        listOf(SharedPreferencesMigration(context, SETTING_PREFERENCES_NAME))
    })

object PreferencesKeys {
    // Note: this has the the same name that we used with SharedPreferences.
    val SOUND = booleanPreferencesKey("SOUND")
    val VIBRATE = booleanPreferencesKey("VIBRATE")
    val SAVE_HISTORY = booleanPreferencesKey("SAVE_HISTORY")
    val REMOVE_ADS = booleanPreferencesKey("REMOVE_ADS")
    val LANGUAGES = stringPreferencesKey("LANGUAGES")
}

class DataStoreHelper @Inject constructor(
    @ApplicationContext val context: Context
) : PrefsStore {
    override fun getSetting(): Flow<SettingPreferences> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                // Get our show completed value, defaulting to false if not set:
                val sound = preferences[PreferencesKeys.SOUND] ?: false
                val vibrate = preferences[PreferencesKeys.VIBRATE] ?: false
                val saveHistory = preferences[PreferencesKeys.SAVE_HISTORY] ?: false
                val removeAds = preferences[PreferencesKeys.REMOVE_ADS] ?: false

                SettingPreferences(sound, vibrate, saveHistory, removeAds)
            }
    }

    override suspend fun enableSetting(key: Preferences.Key<Boolean>, enable: Boolean) {
        // edit handles data transactionally, ensuring that if the sort is updated at the same
        // time from another thread, we won't have conflicts
        context.dataStore.edit { preferences ->
            // Get the current SortOrder as an enum
            preferences[key] = enable
        }
    }

    override suspend fun changeLanguage(lang: String) {
        // edit handles data transactionally, ensuring that if the sort is updated at the same
        // time from another thread, we won't have conflicts
        context.dataStore.setValue(PreferencesKeys.LANGUAGES, lang)
    }

    override fun getLanguage(): Flow<String> {
        return context.dataStore.getValueFlow(PreferencesKeys.LANGUAGES, Languages.DEFAULT)
    }

    override fun <T> DataStore<Preferences>.getValueFlow(
        key: Preferences.Key<T>,
        defaultValue: T
    ): Flow<T> {
        return this.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences[key] ?: defaultValue
            }
    }

    override suspend fun <T> DataStore<Preferences>.setValue(key: Preferences.Key<T>, value: T) {
        this.edit { preferences ->
            preferences[key] = value
        }
    }
}
