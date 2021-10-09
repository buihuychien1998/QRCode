package com.example.qrcode.di.module

import com.example.qrcode.BuildConfig
import com.example.qrcode.data.ApiService
import com.example.qrcode.di.annotation.NetworkInterceptor
import com.example.qrcode.di.annotation.OkHttpClientNetwork
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    @Provides
    @Singleton
    @OkHttpClientNetwork
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        @NetworkInterceptor interceptor: Interceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(0, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .addInterceptor(httpLoggingInterceptor)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideApiService(
        @OkHttpClientNetwork okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor() = HttpLoggingInterceptor().apply {
        val level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        setLevel(level)
    }

    @Provides
    @Singleton
    fun provideGson() = Gson()

    @Provides
    @Singleton
    fun provideGsonConverterFactory(gson: Gson) = GsonConverterFactory.create(gson)

    companion object {
        /**
         * @Provides annotation is used for provide dependency of third party library
         * @Binds annotation is used for binding an interface with implementation
         */
        private const val BASE_URL = "https://5e510330f2c0d300147c034c.mockapi.io/"
    }
}