package com.example.qrcode.presentation.ui.main.qrcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.qrcode.common.ResultWrapper
import com.example.qrcode.data.repository.local.qrcode.QRCodeRepository
import com.example.qrcode.model.entity.Barcode
import com.example.qrcode.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRCodeViewModel @Inject constructor(private val qrCodeRepository: QRCodeRepository) :
    BaseViewModel() {
    private val _qrCodeDeleteAllResult = MutableLiveData<ResultWrapper<Int>>()
    val qrCodeDeleteAllResult: LiveData<ResultWrapper<Int>>
        get() = _qrCodeDeleteAllResult

    private val _qrCodeDeleteResult = MutableLiveData<ResultWrapper<Int>>()
    val qrCodeDeleteResult: LiveData<ResultWrapper<Int>>
        get() = _qrCodeDeleteResult


    val barList: LiveData<ResultWrapper<List<Barcode>>> = flow {
        emit(ResultWrapper.loading())
        qrCodeRepository.barList.collect {
            emit(ResultWrapper.success(it))
        }
    }.asLiveData()

    fun delete(barcode: Barcode) = viewModelScope.launch {
        _qrCodeDeleteResult.postValue(ResultWrapper.loading())
        val result = qrCodeRepository.delete(barcode)
        _qrCodeDeleteResult.postValue(ResultWrapper.success(result))
    }

    fun deleteAll() = viewModelScope.launch {
        _qrCodeDeleteAllResult.postValue(ResultWrapper.loading())
        val result = qrCodeRepository.deleteAll()
        _qrCodeDeleteAllResult.postValue(ResultWrapper.success(result))
    }
}