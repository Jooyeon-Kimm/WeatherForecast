package com.example.bottomnavigation.data


data class DailyWeatherItem(
    var dateDayOfWeek: String,
    var dateNum: String,
    var iconAM: Int,
    var iconPM: Int,
    var rainProbAM: String,
    var rainProbPM: String,
    var minTemp: String,
    var maxTemp: String,
)