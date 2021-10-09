package com.example.qrcode.presentation.ui.main.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.qrcode.common.ResultWrapper
import com.example.qrcode.data.repository.local.history.HistoryRepository
import com.example.qrcode.model.entity.History
import com.example.qrcode.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(private val historyRepository: HistoryRepository) :
    BaseViewModel() {

    private val _historyDeleteAllResult = MutableLiveData<ResultWrapper<Int>>()
    val historyDeleteAllResult: LiveData<ResultWrapper<Int>>
        get() = _historyDeleteAllResult
    private val _historyDeleteResult = MutableLiveData<ResultWrapper<Int>>()
    val historyDeleteResult: LiveData<ResultWrapper<Int>>
        get() = _historyDeleteResult


    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
//    val allHistories: LiveData<List<History>> = historyRepository.allHistories.asLiveData()
    val allHistories: LiveData<ResultWrapper<List<History>>> = flow {
        emit(ResultWrapper.loading())
        delay(500L)
        historyRepository.allHistories.collect {
            emit(ResultWrapper.success(it))
        }
    }.asLiveData()

    fun delete(history: History) = viewModelScope.launch {
        _historyDeleteResult.postValue(ResultWrapper.loading())
        val result = historyRepository.delete(history)
        _historyDeleteResult.postValue(ResultWrapper.success(result))
    }

    fun deleteAll() = viewModelScope.launch {
        _historyDeleteAllResult.postValue(ResultWrapper.loading())
        delay(500L)
        val result = historyRepository.deleteAll()
        _historyDeleteAllResult.postValue(ResultWrapper.success(result))
    }
}