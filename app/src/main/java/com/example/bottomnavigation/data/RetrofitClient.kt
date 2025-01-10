package com.example.bottomnavigation.data

import com.example.bottomnavigation.models.AddressApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://business.juso.go.kr/"

    val instance: AddressApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AddressApiService::class.java)
    }
}