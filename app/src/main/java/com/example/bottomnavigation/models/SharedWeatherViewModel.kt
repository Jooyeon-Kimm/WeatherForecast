package com.example.bottomnavigation.models

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bottomnavigation.R
import com.example.bottomnavigation.data.DailyWeatherItem
import com.example.bottomnavigation.data.FavoriteAddress
import com.example.bottomnavigation.data.HourlyWeather
import com.example.bottomnavigation.data.WeatherDTO
import com.google.gson.Gson
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class SharedWeatherViewModel : ViewModel() {
    val weatherData = MutableLiveData<WeatherDTO>() // 메인 페이지의 현재 날씨 데이터 1개
    val dailyWeatherItems = MutableLiveData<List<DailyWeatherItem>>() // HomeBottomFrame의 일간 날씨 리스트
    val weatherDescription = MutableLiveData<String>()
    val currentLocation: MutableLiveData<String> = MutableLiveData()
    val currentLocationLong:MutableLiveData<String> = MutableLiveData()
    private val _isBookmarked = MutableLiveData<Boolean>()
    val isBookmarked: LiveData<Boolean> = _isBookmarked
    private val _addresses = MutableLiveData<List<FavoriteAddress>>()
    val addresses: LiveData<List<FavoriteAddress>> = _addresses
    val latitude = MutableLiveData<String>()
    val longitude = MutableLiveData<String>()
    val address = MutableLiveData<String>()
    val addressLong = MutableLiveData<String>()

    // 데이터 업데이트 메서드
    fun updateLocationData(lat: String, lon: String, addr: String, addrLong: String) {
        latitude.value = lat
        longitude.value = lon
        address.value = addr
        addressLong.value = addrLong
    }

    // 북마크 상태 업데이트
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
            currentList[index] = currentList[index].copy(isBookmarked = false)
        }

        _addresses.value = currentList
    }



    // 특정 주소가 북마크인지 확인
    fun isAddressBookmarked(location: String): Boolean {
        return _addresses.value.orEmpty().any { it.title == location && it.isBookmarked }
    }

    // 북마크 목록 반환
    fun getBookmarkedAddresses(): List<FavoriteAddress> {
        return _addresses.value.orEmpty().filter { it.isBookmarked }
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

    // 데이터를 업데이트하는 함수
    fun updateWeatherData(data: WeatherDTO) {
        weatherData.value = data
        weatherDescription.value = data.current.weather[0].description
        // 잘 받아왔는지 확인 (OK)
        // Log.e("JOO", "${weatherData.value?.current?.temp }")


        val items = data.daily?.take(7)?.map { daily ->
            val dailyDate = convertUnixTimeToLocalDate(daily.dt)
            val dailyDateFormatted = convertUnixTimeToFormattedDate(daily.dt)

            val rainProbAM = getRainProbabilityForHour(data.hourly, dailyDate, 9)
            val rainProbPM = getRainProbabilityForHour(data.hourly, dailyDate, 18)
            Log.d("JOO", rainProbPM)
            Log.d("JOO", rainProbAM)
            val iconAM = getIconForTime(data.hourly, dailyDate, 9) // 오전 9시에 해당하는 아이콘
            val iconPM = getIconForTime(data.hourly, dailyDate, 18) // 오후 6시에 해당하는 아이콘


            DailyWeatherItem(
                dateDayOfWeek = getDayOfWeek(daily.dt),
                dateNum = dailyDateFormatted,
                iconAM = iconAM,
                iconPM = iconPM,
                rainProbAM = rainProbAM,
                rainProbPM = rainProbPM,
                minTemp = daily.temp.min.toInt().toString(),
                maxTemp = daily.temp.max.toInt().toString()
            )
        } ?: emptyList()


        dailyWeatherItems.value = items // 일간 날씨 아이템 리스트 업데이트
        Log.e("JOOICON", items[0].iconAM.toString())
        Log.e("JOO", "min0: " + items[0].minTemp)
        Log.e("JOO", "min1: " + items[1].minTemp)
        Log.e("JOO", "min2: " + items[2].minTemp)
        Log.e("JOO", "min3: " + items[3].minTemp)
        Log.e("JOO", "min4: " + items[4].minTemp)

        Log.d("JOO", "Updated items with size: ${items.size}")
        if (items.isEmpty()) {
            Log.e("JOO", "No daily data available or processing failed.")
        }
    }

    fun updateLocation(location: String) {
        currentLocation.value = location
    }


    // Unix 시간 : 1970년 1월 1일 00:00 UTC로부터 경과한 시간 (단위: 초)
    // 날짜가져오기 (Unix에서 LocalDate형)
    fun convertUnixTimeToLocalDate(unixTime: Long): LocalDate {
        return Instant.ofEpochSecond(unixTime) // Convert Unix timestamp to an Instant
            .atZone(ZoneId.systemDefault()) // Apply the system default time zone
            .toLocalDate() // Convert to LocalDate
    }

    // 날짜 형식 적용 String 반환
    fun convertUnixTimeToFormattedDate(unixTime: Long): String {
        val formatter = DateTimeFormatter.ofPattern("MM/dd")  // 원하는 날짜 형식 설정
        return Instant.ofEpochSecond(unixTime)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }

    // Unix 시간 > 요일
    fun getDayOfWeek(unixSeconds: Long): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE") // Full name of the day
        val engDayofWeek = Instant.ofEpochSecond(unixSeconds)
            .atZone(ZoneId.systemDefault())
            .format(formatter)

        Log.d("요일: ", engDayofWeek)
        return when(engDayofWeek){
            "Monday" -> "월"
            "Tuesday" -> "화"
            "Wednesday" -> "수"
            "Thursday" -> "목"
            "Friday" -> "금"
            "Saturday" -> "토"
            "Sunday" -> "일"
            else -> "N/A"
        }
    }


    // 아이콘 이름 : 리소스 파일 매칭
    fun getIconResourceId(iconCode: String): Int {
        return when (iconCode) {
            "01d" -> R.drawable.w01d
            "02d" -> R.drawable.w02d
            "03d" -> R.drawable.w03d
            "04d" -> R.drawable.w04d
            "09d" -> R.drawable.w09d
            "10d" -> R.drawable.w10d
            "11d" -> R.drawable.w11d
            "13d" -> R.drawable.w13d
            "50d" -> R.drawable.w50d

            "01n" -> R.drawable.w01n
            "02n" -> R.drawable.w02n
            "03n" -> R.drawable.w03n
            "04n" -> R.drawable.w04n
            "09n" -> R.drawable.w09n
            "10n" -> R.drawable.w10n
            "11n" -> R.drawable.w11n
            "13n" -> R.drawable.w13n
            "50n" -> R.drawable.w50n

            else -> R.drawable.star
        }
    }

    fun getRainProbabilityForHour(hourlyData: List<HourlyWeather>?, date: LocalDate, hour: Int): String {
        return hourlyData
            ?.filter { hourly ->
                val hourlyDate = Instant.ofEpochSecond(hourly.dt).atZone(ZoneId.systemDefault()).toLocalDate()
                val hourlyHour = Instant.ofEpochSecond(hourly.dt).atZone(ZoneId.systemDefault()).hour
                hourlyDate.isEqual(date) && hourlyHour == hour
            }
            ?.map { (it.pop * 100).toInt().toString() }
            ?.firstOrNull() ?: "0"
    }


    private fun getIconForTime(hourlyData: List<HourlyWeather>?, date: LocalDate, hour: Int): Int {
        // 해당 날짜와 시간에 맞는 첫 번째 아이콘 코드 찾기
        val iconCode = hourlyData
            ?.firstOrNull { hourly ->
                val hourlyDateTime = Instant.ofEpochSecond(hourly.dt).atZone(ZoneId.systemDefault())
                val hourlyDate = hourlyDateTime.toLocalDate()
                val hourlyHour = hourlyDateTime.hour
                hourlyDate.isEqual(date) && hourlyHour == hour
            }
            ?.weather?.firstOrNull()?.icon

        // 아이콘 코드를 기반으로 리소스 ID 반환
        return getIconResourceId(iconCode ?: "02d")
    }






}
