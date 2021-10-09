package com.example.qrcode.di.module

import com.example.qrcode.common.DataStoreHelper
import com.example.qrcode.data.repository.remote.MainDataSource
import com.example.qrcode.data.repository.remote.MainRepository
import com.example.qrcode.datastore.PrefsStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun provideAuthDataSource(mainRepository: MainRepository): MainDataSource

    @Binds
    @Singleton
    abstract fun provideSettingPreferences(dataStoreHelper: DataStoreHelper): PrefsStore
}