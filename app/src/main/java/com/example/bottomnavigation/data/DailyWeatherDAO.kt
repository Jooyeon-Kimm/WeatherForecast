package com.example.bottomnavigation.data

import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

// Dao (Data Access Object) : Interface
// 메서드 정의 (테이블에 데이터 삽입, 수정, 삭제)
@Dao
interface DailyWeatherDAO {
    @Insert
    fun insert(itemW: WeatherDTO)

    @Update
    fun update(itemW: WeatherDTO)

    @Delete
    fun delete(itemW: WeatherDTO)

    // 모든 데이터 가져오기
    @Query("SELECT * FROM WeatherDTO")
    fun getALl(): List<WeatherDTO>

    // 첫 번째 데이터 가져오기
    @Query("SELECT * FROM WeatherDTO ORDER BY id ASC LIMIT 1")
    fun getFirstItem(): WeatherDTO

    // 특정 날짜 찾아서 해당 데이터 삭제하기
    @Query("DELETE FROM WeatherDTO WHERE lat = :lat")
    fun deleteWeatherItemByLat(lat: Double)
}