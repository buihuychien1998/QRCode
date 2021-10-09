package com.example.qrcode.data

import com.example.qrcode.model.User
import retrofit2.http.GET

interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<User>
}