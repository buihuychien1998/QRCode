package com.example.qrcode.di.module

import com.example.qrcode.data.config.CustomInterceptor
import com.example.qrcode.di.annotation.NetworkInterceptor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InterceptorModule {
    /**
     * Handle network interceptor
     *
     *
     * ネットワークインターセプターを処理する
     */
    @Binds
    @Singleton
    @NetworkInterceptor
    abstract fun provideNetworkInterceptor(interceptor: CustomInterceptor): Interceptor
}