package com.domcheung.fittrackpro.di

import com.domcheung.fittrackpro.data.remote.WgerApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Hilt module for providing network-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val WGER_API_BASE_URL = "https://wger.de/api/v2/"

    /**
     * Provides a singleton OkHttpClient instance.
     * Includes a logging interceptor to see network request details in Logcat during debug builds.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Provides a singleton Retrofit instance.
     * @param okHttpClient The OkHttpClient to be used for requests.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(WGER_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides a singleton WgerApiService instance, created by Retrofit.
     * @param retrofit The Retrofit instance to create the service.
     */
    @Provides
    @Singleton
    fun provideWgerApiService(retrofit: Retrofit): WgerApiService {
        return retrofit.create(WgerApiService::class.java)
    }
}