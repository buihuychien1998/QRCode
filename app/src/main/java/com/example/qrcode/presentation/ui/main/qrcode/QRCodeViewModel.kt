package com.example.qrcode.presentation.ui.main.qrcode

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.qrcode.common.ResultWrapper
import com.example.qrcode.data.repository.local.qrcode.QRCodeRepository
import com.example.qrcode.model.entity.Barcode
import com.example.qrcode.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class QRCodeViewModel @Inject constructor(private val qrCodeRepository: QRCodeRepository) :
    BaseViewModel() {
    private val _qrCodeDeleteAllResult = MutableLiveData<ResultWrapper<Int>?>()
    val qrCodeDeleteAllResult: LiveData<ResultWrapper<Int>?>
        get() = _qrCodeDeleteAllResult

    private val _qrCodeDeleteResult = MutableLiveData<ResultWrapper<Int>?>()
    val qrCodeDeleteResult: LiveData<ResultWrapper<Int>?>
        get() = _qrCodeDeleteResult

    private val _insertImageResult = MutableLiveData<ResultWrapper<String>?>()
    val insertImageResult: LiveData<ResultWrapper<String>?>
        get() = _insertImageResult


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

    suspend fun insertBitmap(
        contentResolver: ContentResolver?,
        image: ByteArray,
        title: String
    ): String {
        // Move the execution of the coroutine to the I/O dispatcher
        // Blocking network request code
        _insertImageResult.postValue(ResultWrapper.loading())
        return withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
            val bytes = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(
                contentResolver,
                bitmap,
                title,
                null
            )
            _insertImageResult.postValue(ResultWrapper.success(path))
            path
        }
    }

    suspend fun deleteBitmap(contentResolver: ContentResolver?, imageUri: Uri?): Int {
        // Move the execution of the coroutine to the I/O dispatcher
        // Blocking network request code
        return withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            val deleted = imageUri?.let {
                contentResolver?.delete(it, null, null)
            } ?: 0
            if (deleted > 0) {
                Timber.d("File deleted")
            }
            deleted
        }
    }

    fun clearData() {
        _qrCodeDeleteResult.value = null
    }
}