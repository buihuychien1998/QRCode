package com.store.qrcode.di.module

import com.store.qrcode.common.DataStoreHelper
import com.store.qrcode.data.repository.remote.MainDataSource
import com.store.qrcode.data.repository.remote.MainRepository
import com.store.qrcode.datastore.PrefsStore
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