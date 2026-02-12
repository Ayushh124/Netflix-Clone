package com.netflixclone.network.models

import com.netflixclone.network.services.ApiService
import retrofit2.Retrofit
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient

import java.net.CookieManager
import java.net.CookiePolicy
import okhttp3.JavaNetCookieJar

object RetrofitClient {

    private val BASE_URL = com.netflixclone.BuildConfig.BASE_URL

    private fun getOkHttpClient(): OkHttpClient {
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        return OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()
    }

    private val client = getOkHttpClient()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(client)
        .build()
        .create(ApiService::class.java)
    }
}
