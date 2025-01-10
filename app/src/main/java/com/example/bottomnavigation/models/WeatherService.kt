package com.example.bottomnavigation.models

import com.example.bottomnavigation.data.WeatherDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit 인터페이스
interface WeatherService {

    // 각각 다른 API 엔드포인트를 정의하면 된다.
    // https://api.openweathermap.org/data/2.5/weather?q=Seoul&appid={API_KEY}
    // @Query 어노테이션을 사용해 질의 문자열 지정


    // 250108 OneCall 구독 후, BaseURL 2.5 > 3.0으로 변경
    // https://openweathermap.org/api/one-call-3
    // [1] 현재 날씨와 날씨예보 ( Current and forecasts weather data )
    @GET("onecall")
    fun getWeather(
        @Query("lat") lat: Double,  // 경도
        @Query("lon") lon: Double, // 위도
        @Query("appid") appid: String, // api_key
        @Query("units") units: String,  // 온도 단위(unit)
    ): Call<WeatherDTO> // Call/Response<데이터 저장 모델 클래스> (Call: 비동기, Response: 동기)


}


// suspend fun
// 정의) 함수 내에 일시 중단 지점을 포함할 수 있는 함수
// 역할) 실행되는 일시중단 지점이 포함된 코드들을 재사용할 수 있는 코드의 집합으로 만듦
// Coroutine   : 언제든지 일시 중단하고, 스레드를 양보할 수 있음