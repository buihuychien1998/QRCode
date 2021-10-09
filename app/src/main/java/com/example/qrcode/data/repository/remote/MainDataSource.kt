package com.example.qrcode.data.repository.remote

import com.example.qrcode.model.User

interface MainDataSource {
    suspend fun getUsers(): List<User>
}