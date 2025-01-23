package com.example.bottomnavigation.data


data class DailyWeatherItem(
    var dateDayOfWeek: String,
    var dateNum: String,
    var rainProbAM: String,
    var rainProbPM: String,
    var minTemp: String,
    var maxTemp: String,
)