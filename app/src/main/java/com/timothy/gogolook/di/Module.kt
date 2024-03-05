package com.timothy.gogolook.di

import com.timothy.gogolook.data.local.SearchTermsHistoryService
import com.timothy.gogolook.data.local.SearchTermsHistoryImpl
import com.timothy.gogolook.data.network.PixabayService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ModuleProvide {
    @Singleton
    @Provides
    fun providePixabayService(): PixabayService {
        val baseURL = "https://pixabay.com/"

        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PixabayService::class.java)
    }
}

@InstallIn(SingletonComponent::class)
@Module
abstract class ModuleBinds {
    @Singleton
    @Binds
    abstract fun provideSearchTermsHistoryService(impl:SearchTermsHistoryImpl): SearchTermsHistoryService
}
