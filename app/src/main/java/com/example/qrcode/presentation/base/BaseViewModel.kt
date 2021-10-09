package com.example.qrcode.presentation.base

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.qrcode.common.ResultWrapper
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineExceptionHandler

open class BaseViewModel : ViewModel() {
    private val isLoading = MutableLiveData<Boolean>()
    protected val compositeDisposable = CompositeDisposable()

    fun getIsLoading() = isLoading

    fun postLoading(loading: Boolean) {
        isLoading.postValue(loading)
    }

    fun setLoading(loading: Boolean) {
        isLoading.value = loading
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}