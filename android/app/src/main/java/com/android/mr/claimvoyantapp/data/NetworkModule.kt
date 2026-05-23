package com.android.mr.claimvoyantapp.data

import com.android.mr.claimvoyantapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    /** Injected at build time from local.properties → BuildConfig.BACKEND_BASE_URL */
    val BASE_URL: String = BuildConfig.BACKEND_BASE_URL

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)   // no timeout — SSE streams stay open
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val claimApi: ClaimApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ClaimApiService::class.java)
}
