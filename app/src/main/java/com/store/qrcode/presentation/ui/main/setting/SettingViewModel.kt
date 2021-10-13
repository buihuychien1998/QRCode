package com.store.qrcode.presentation.ui.main.setting

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.store.qrcode.datastore.PrefsStore
import com.store.qrcode.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(val prefsStore: PrefsStore): BaseViewModel() {
    val setting = prefsStore.getSetting().asLiveData()
    val language = prefsStore.getLanguage().asLiveData()

    fun enableSetting(key: Preferences.Key<Boolean>, enable: Boolean){
        viewModelScope.launch {
            prefsStore.enableSetting(key, enable)
        }
    }

    fun changeLanguage(lang: String){
        viewModelScope.launch {
            prefsStore.changeLanguage(lang)
        }
    }

}