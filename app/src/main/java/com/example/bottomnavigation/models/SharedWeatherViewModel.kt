package com.example.bottomnavigation.models

import android.app.Application
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bottomnavigation.R
import com.example.bottomnavigation.data.DailyWeatherItem
import com.example.bottomnavigation.data.FavoriteAddress
import com.example.bottomnavigation.data.HourlyWeather
import com.example.bottomnavigation.data.WeatherDTO
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


// 날씨 데이터, 북마크 아이템 (addresses)
// 앱 실행 중 변경이 잦은 데이터들
class SharedWeatherViewModel(application: Application) : AndroidViewModel(application){
    val weatherData = MutableLiveData<WeatherDTO>() // 메인 페이지의 현재 날씨 데이터 1개
    val dailyWeatherItems = MutableLiveData<List<DailyWeatherItem>>() // HomeBottomFrame의 일간 날씨 리스트
    val weatherDescription = MutableLiveData<String>()
    val currentLocation: MutableLiveData<String> = MutableLiveData()
    val currentLatitude:MutableLiveData<Double> = MutableLiveData()
    val currentLongitude:MutableLiveData<Double> = MutableLiveData()
    private val _addresses = MutableLiveData<List<FavoriteAddress>>()
    val addresses: LiveData<List<FavoriteAddress>> = _addresses

    private val sharedPreferences = application.getSharedPreferences("WeatherAppPreferences", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        loadAddresses()
    }


    // sharedPreferences에 북마크된 장소들 저장
    private fun loadAddresses() {
        val addressesJson = sharedPreferences.getString("savedAddresses", null) // 저장된 JSON 가져오기
        if (addressesJson != null) {
            val type = object : TypeToken<List<FavoriteAddress>>() {}.type
            val loadedAddresses: List<FavoriteAddress> = gson.fromJson(addressesJson, type) // JSON을 객체 리스트로 변환
            _addresses.postValue(loadedAddresses)
        } else {
            _addresses.postValue(emptyList()) // 초기 값 설정
        }
    }





    fun updateBookmarkState(location: String, isBookmarked: Boolean) {
        val currentList = _addresses.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.title == location } // 없으면 -1 반환

        if (isBookmarked) {
            if (index == -1) {
                // 주소가 목록에 없으면 새로 추가
                currentList.add(FavoriteAddress(title = location, descr = location, isBookmarked = true, isChecked = false))
            } else {
                // 이미 존재하는 경우, 상태만 업데이트
                currentList[index] = currentList[index].copy(isBookmarked = true)
            }
        } else {
            if (index != -1) {
                // 아이템을 목록에서 제거
                currentList.removeAt(index)
            }
        }

        _addresses.value = currentList
    }




    // 특정 주소가 북마크인지 확인
    fun isAddressBookmarked(location: String): Boolean {
        return _addresses.value.orEmpty().any { it.title == location && it.isBookmarked }
    }


    fun updateAddresses(newAddresses: List<FavoriteAddress>) {
        _addresses.value = newAddresses
    }


    // SharedPreference에 저장
    fun saveAddresses(context: Context) {
        val sharedPreferences = context.getSharedPreferences("sp", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            val json = Gson().toJson(_addresses.value)
            putString("address_list", json)
            apply()
        }
    }


    fun loadAddresses(context: Context) {
        val sharedPreferences = context.getSharedPreferences("sp", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("address_list", null)

        if (json != null) {
            val type = object : TypeToken<MutableList<FavoriteAddress>>() {}.type
            val loadedAddresses: MutableList<FavoriteAddress> = Gson().fromJson(json, type)
            _addresses.postValue(loadedAddresses) // LiveData 업데이트
        } else {
            _addresses.postValue(mutableListOf()) // 빈 리스트로 초기화
        }
    }


    // 데이터를 업데이트하는 함수
    fun updateWeatherData(data: WeatherDTO) {
        weatherData.value = data
        weatherDescription.value = data.current.weather[0].description

        val items = data.daily?.take(8)?.mapIndexed { index, daily ->
            val dailyDate = convertUnixTimeToLocalDate(daily.dt)
            val dailyDateFormatted = convertUnixTimeToFormattedDate(daily.dt)

            val rainProbAM = ((data.hourly!![4].pop) * 100).toInt().toString() // 오전 5시
            val rainProbPM = ((data.hourly[16].pop) * 100).toInt().toString() // 오후 5시

            Log.d("JOO_rainProbPM", rainProbPM)
            Log.d("JOO_rainProbAM", rainProbAM)

            for(i:Int in 0..23){
                Log.d("JOO_rainProb[$i] :", "${(data.hourly[i].pop*100).toInt()}")
            }

            val dayOfWeek = getDayOfWeek(daily.dt) // 요일 받아오기
            val displayDayOfWeek = if (index == 0) "$dayOfWeek(오늘)" else dayOfWeek // 화면에 표시할 요일 (오늘 표시)


            DailyWeatherItem(
                dateDayOfWeek = displayDayOfWeek,
                dateNum = dailyDateFormatted,
                rainProbAM = rainProbAM,
                rainProbPM = rainProbPM,
                minTemp = daily.temp.min.toInt().toString(),
                maxTemp = daily.temp.max.toInt().toString()
            )
        } ?: emptyList()



        dailyWeatherItems.value = items // 일간 날씨 아이템 리스트 업데이트
        Log.d("JOO", "가져온 날씨데이터 개수는 ${items.size}개")
        if (items.isEmpty()) {
            Log.e("JOO", "날씨데이터가 비어있음")
        }
    }



    // Unix 시간 : 1970년 1월 1일 00:00 UTC로부터 경과한 시간 (단위: 초)
    // 날짜가져오기 (Unix에서 LocalDate형)
    private fun convertUnixTimeToLocalDate(unixTime: Long): LocalDate {
        return Instant.ofEpochSecond(unixTime) // Unix timestamp를 Instant로 변경
            .atZone(ZoneId.systemDefault()) // system default time zone
            .toLocalDate() // LocalDate 로 변경
    }

    // 날짜 형식 적용 String 반환
    private fun convertUnixTimeToFormattedDate(unixTime: Long): String {
        val formatter = DateTimeFormatter.ofPattern("MM/dd")  // 원하는 날짜 형식 설정
        return Instant.ofEpochSecond(unixTime)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }

    // Unix 시간 > 요일
    private fun getDayOfWeek(unixSeconds: Long): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE") // 데이터 형식
        val engDayofWeek = Instant.ofEpochSecond(unixSeconds)
            .atZone(ZoneId.systemDefault())
            .format(formatter)

        Log.d("요일: ", engDayofWeek)
        return when(engDayofWeek){
            "월요일" -> "월"
            "화요일" -> "화"
            "수요일" -> "수"
            "목요일" -> "목"
            "금요일" -> "금"
            "토요일" -> "토"
            "일요일" -> "일"
            else -> "N/A"
        }
    }
}