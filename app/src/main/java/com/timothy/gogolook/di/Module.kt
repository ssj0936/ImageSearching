package com.timothy.gogolook.di

import com.timothy.gogolook.MainApp.Companion.appContext
import com.timothy.gogolook.data.SearchTermsHistoryService
import com.timothy.gogolook.data.local.SearchTermsHistoryLocal
import com.timothy.gogolook.data.network.PixabayService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object Module {
    @Singleton
    @Provides
    fun providePixabayService(): PixabayService {
        val baseURL = "https://pixabay.com/"

        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(PixabayService::class.java)
    }

    @Singleton
    @Provides
    fun provideSearchTermsHistoryService(): SearchTermsHistoryService {
        return SearchTermsHistoryLocal(appContext)
    }
}