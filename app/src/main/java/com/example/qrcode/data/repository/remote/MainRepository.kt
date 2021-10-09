package com.example.qrcode.data.repository.remote

import com.example.qrcode.data.ApiService
import javax.inject.Inject

class MainRepository @Inject constructor(private val apiService: ApiService) : MainDataSource {
    override suspend fun getUsers() = apiService.getUsers()
}