package com.store.qrcode.presentation.ui.main.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.store.qrcode.common.ResultWrapper
import com.store.qrcode.data.repository.local.qrcode.QRCodeRepository
import com.store.qrcode.model.entity.Barcode
import com.store.qrcode.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(private val qrCodeRepository: QRCodeRepository) :
    BaseViewModel() {

    private val _qrCodeInsertResult = MutableLiveData<ResultWrapper<Long>>()
    val qrCodeInsertResult: LiveData<ResultWrapper<Long>>
        get() = _qrCodeInsertResult

    private val _qrCodeUpdateResult = MutableLiveData<ResultWrapper<Int>>()
    val qrCodeUpdateResult: LiveData<ResultWrapper<Int>>
        get() = _qrCodeUpdateResult

    fun insert(barcode: Barcode) = viewModelScope.launch {
        _qrCodeInsertResult.postValue(ResultWrapper.loading())
        val result = qrCodeRepository.insert(barcode)
        _qrCodeInsertResult.postValue(ResultWrapper.success(result))
    }

    fun update(qrCode: String?, image: ByteArray?, genre: String, id: Int) = viewModelScope.launch {
        _qrCodeUpdateResult.postValue(ResultWrapper.loading())
        val result = qrCodeRepository.update(qrCode, image, genre, id)
        _qrCodeUpdateResult.postValue(ResultWrapper.success(result))
    }
}