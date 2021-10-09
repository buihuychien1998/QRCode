package com.example.qrcode.presentation.ui.main.setting

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.qrcode.common.DataStoreHelper
import com.example.qrcode.common.PreferencesKeys
import com.example.qrcode.datastore.PrefsStore
import com.example.qrcode.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(val prefsStore: PrefsStore): BaseViewModel() {
    val setting = prefsStore.getSetting().asLiveData()

    fun enableSetting(key: Preferences.Key<Boolean>, enable: Boolean){
        viewModelScope.launch {
            prefsStore.enableSetting(key, enable)
        }
    }

}