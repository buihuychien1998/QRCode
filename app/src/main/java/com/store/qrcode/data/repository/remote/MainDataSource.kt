package com.store.qrcode.data.repository.remote

import com.store.qrcode.model.User

interface MainDataSource {
    suspend fun getUsers(): List<User>
}