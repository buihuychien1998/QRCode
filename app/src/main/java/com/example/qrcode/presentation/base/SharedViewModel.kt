package com.example.qrcode.presentation.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SharedViewModel : BaseViewModel() {
    //     Encapsulate the LiveData
    //    MutableLiveData vs. LiveData:
    //    Data in a MutableLiveData object can be changed, as the name implies.
    //    Inside the ViewModel, the data should be editable, so it uses MutableLiveData.
    //Data in a LiveData object can be read, but not changed.
    // From outside the ViewModel, data should be readable, but not editable,
    // so the data should be exposed as LiveData.

//    private val _enableQRDetect = MutableLiveData<Boolean>()
//    val enableQRDetect: LiveData<Boolean>
//        get() = _enableQRDetect

    private var enableQRDetect = true

    fun enableQRDetect(enable: Boolean) {
        enableQRDetect = enable
    }

    fun isEnableQRDetect() = enableQRDetect

    private val _imagePath = MutableLiveData<String?>()
    val imagePath: LiveData<String?>
        get() = _imagePath

    fun setImagePath(path: String?){
        _imagePath.value = path
    }
}