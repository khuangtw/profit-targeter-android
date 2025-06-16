package com.example.profittargeter.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://option-strategy-api-695533194214.us-central1.run.app"

    // 1. 建立带 KotlinJsonAdapterFactory 的 Moshi
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: StrategyService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // 2. 用我们自定义的 moshi
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(StrategyService::class.java)
    }
}
