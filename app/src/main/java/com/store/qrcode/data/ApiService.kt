package com.store.qrcode.data

import com.store.qrcode.model.User
import retrofit2.http.GET

interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<User>
}