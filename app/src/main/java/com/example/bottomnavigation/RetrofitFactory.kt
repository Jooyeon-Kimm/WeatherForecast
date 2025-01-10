package com.example.bottomnavigation

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 싱글톤으로 Retrofit 객체 생성
object RetrofitFactory {
    private const val BASE_URL = "https://api.openweathermap.org/data/3.0/"
    private var instance: Retrofit? = null

    fun getInstance(): Retrofit {
        if (instance == null) {
            instance = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return instance!!
    }
}