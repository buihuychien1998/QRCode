package com.example.qrcode.presentation.ui.main.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.qrcode.common.ResultWrapper
import com.example.qrcode.data.repository.local.history.HistoryRepository
import com.example.qrcode.datastore.PrefsStore
import com.example.qrcode.model.entity.History
import com.example.qrcode.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    prefsStore: PrefsStore
) : BaseViewModel() {
    val setting = prefsStore.getSetting().asLiveData()
    private val _historyInsertResult = MutableLiveData<ResultWrapper<Long>>()
    val historyInsertResult: LiveData<ResultWrapper<Long>>
        get() = _historyInsertResult

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(history: History) = viewModelScope.launch {
        _historyInsertResult.postValue(ResultWrapper.loading())
        val result = historyRepository.insert(history)
        _historyInsertResult.postValue(ResultWrapper.success(result))
    }
}