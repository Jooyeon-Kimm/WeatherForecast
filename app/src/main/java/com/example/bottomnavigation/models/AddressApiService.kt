package com.example.bottomnavigation.models

import com.example.bottomnavigation.data.AddressResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AddressApiService {
    @GET("addrlink/addrLinkApi.do")
    fun getAddress(
        @Query("currentPage") currentPage: String,
        @Query("countPerPage") countPerPage: String,
        @Query("keyword") keyword: String,
        @Query("confmKey") confmKey: String,
        @Query("resultType") resultType: String = "json"
    ): Call<AddressResponse>
}
