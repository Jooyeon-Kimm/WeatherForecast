package com.example.bottomnavigation.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// WeatherDTO.kt : 데이터 모델 정의

// Entity: 데이터베이스 테이블 과 비슷한 것
@Entity
data class WeatherDTO(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val timezone_offset: Int,
    val current: CurrentWeather,
    val minutely: List<MinutelyWeather>?,
    val hourly: List<HourlyWeather>?,
    val daily: List<DailyWeather>?,
    val alerts: List<WeatherAlert>?
){
    // 키 값: 오토제너레이터로 자동으로 키 값 생성
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}



// 참고
// Entity이름 지정해주고 싶으면
// @Entity(tableName="userProfile")


data class CurrentWeather(
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val temp: Double,
    val feels_like: Double,
    val pressure: Int,
    val humidity: Int,
    val dew_point: Double,
    val uvi: Double,
    val clouds: Int,
    val visibility: Int,
    val wind_speed: Double,
    val wind_deg: Int,
    val wind_gust: Double?,
    val weather: List<WeatherDescription>
)

data class MinutelyWeather(
    val dt: Long,
    val precipitation: Double
)

data class HourlyWeather(
    val dt: Long,
    val temp: Double,
    val feels_like: Double,
    val pressure: Int,
    val humidity: Int,
    val dew_point: Double,
    val uvi: Double?,
    val clouds: Int,
    val visibility: Int?,
    val wind_speed: Double,
    val wind_deg: Int,
    val wind_gust: Double?,
    val weather: List<WeatherDescription>,
    val pop: Double,
    val snow: SnowInfo?
)

data class DailyWeather(
    val dt: Long,
    val sunrise: Int,
    val sunset: String,
    val moonrise: Int,
    val moonset: String,
    val moon_phase: String,
    val summary: String?,
    val temp: Temperature,
    val feels_like: FeelsLikeTemperature,
    val pressure: Int,
    val humidity: Int,
    val dew_point: Double,
    val wind_speed: Double,
    val wind_deg: Int,
    val wind_gust: Double?,
    val weather: List<WeatherDescription>,
    val clouds: Int,
    val pop: Double,
    val snow: Double?,
    val uvi: Double
)

data class WeatherDescription(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class SnowInfo(
    val `1h`: Double
)

data class Temperature(
    val day: Double,
    val min: Double,
    val max: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)

data class FeelsLikeTemperature(
    val day: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)

data class WeatherAlert(
    val sender_name: String,
    val event: String,
    val start: Long,
    val end: Long,
    val description: String,
    val tags: List<String>
)
