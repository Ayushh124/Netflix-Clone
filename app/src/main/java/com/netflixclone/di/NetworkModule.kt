package com.netflixclone.di

import com.netflixclone.network.services.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val BASE_URL = com.netflixclone.BuildConfig.BASE_URL

    @Provides
    @Singleton
    fun provideOkHttpClient(): okhttp3.OkHttpClient {
        val cookieManager = java.net.CookieManager()
        cookieManager.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL)
        
        // Set as default CookieHandler so ExoPlayer can access the same cookies
        java.net.CookieHandler.setDefault(cookieManager)
        
        return okhttp3.OkHttpClient.Builder()
            .cookieJar(okhttp3.JavaNetCookieJar(cookieManager))
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi, okHttpClient: okhttp3.OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
