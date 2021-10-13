package com.store.qrcode.di.module

import android.content.Context
import com.store.qrcode.data.QRCodeDatabase
import com.store.qrcode.data.repository.local.history.HistoryRepository
import com.store.qrcode.data.repository.local.qrcode.QRCodeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class LocalModule {
    @Provides
    @Singleton
    fun provideQRCodeDatabase(@ApplicationContext context: Context) =
        QRCodeDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideHistoryRepository(qrCodeDatabase: QRCodeDatabase) =
        HistoryRepository(qrCodeDatabase.historyDao())

    @Provides
    @Singleton
    fun provideQRCodeRepository(qrCodeDatabase: QRCodeDatabase) =
        QRCodeRepository(qrCodeDatabase.qrCodeDao())
}