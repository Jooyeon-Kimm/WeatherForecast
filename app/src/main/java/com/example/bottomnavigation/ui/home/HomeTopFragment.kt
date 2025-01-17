package com.example.bottomnavigation.ui.home

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.bottomnavigation.BuildConfig
import com.example.bottomnavigation.BookmarkFragment
import com.example.bottomnavigation.R
import com.example.bottomnavigation.SearchFragment
import com.example.bottomnavigation.data.RetrofitFactory
import com.example.bottomnavigation.models.SharedWeatherViewModel
import com.example.bottomnavigation.data.WeatherDTO
import com.example.bottomnavigation.models.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

class HomeTopFragment : Fragment() {
    private lateinit var sharedPreferences : SharedPreferences
    private val sharedWeatherViewModel: SharedWeatherViewModel by activityViewModels()
    private var currentAddress: String? = null
    private lateinit var providerClient: FusedLocationProviderClient
    private var updateHandler = Handler(Looper.getMainLooper())
    private lateinit var updateRunnable: Runnable
    private lateinit var temperatureTextView: TextView
    private lateinit var tempFeelTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var precipitationTextView: TextView
    private lateinit var windDirectionTextView: TextView
    private lateinit var windSpeedTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var dateView: TextView
    private lateinit var locationButton: TextView
    private lateinit var searchButton : ImageButton
    private lateinit var bookmarkButton: ImageButton
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var lastMinute: Int = -1


    // ■
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("LC_HTF_onCreateView", "htf onCreateView")
        return inflater.inflate(R.layout.fragment_home_top, container, false)
    }

    // ■
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)


        // 위치 > 날씨 > 시간 업데이트
        providerClient = LocationServices.getFusedLocationProviderClient(requireContext())
        observeBookmarkStates()
        observeLocationChanges()
        startFetchingLocationData()
        setupWeatherUpdate()
        startUpdatingDateTime()
        Log.d("LC_HTF_onViewCreated", "htf onViewCreated")
    }


    // ■
    override fun onPause() {
        super.onPause()
        Log.d("LC_HTF_onPause", "htf onPause")
        updateHandler.removeCallbacks(updateRunnable)
        // Fragment가 보이지 않을 때 1분마다 갱신 중단
    }



    // ■
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("LC_HTF_onDestroyView", "htf onDestroyView")

        // 뷰가 파괴될 때 업데이트 중단
        stopUpdatingDateTime()
    }


    private fun observeBookmarkStates() {
        sharedWeatherViewModel.addresses.observe(viewLifecycleOwner) { addresses ->
            // 현재 Address에 해당하는 북마크 상태를 확인
            val isBookmarked = addresses.any { it.title == currentAddress && it.isBookmarked }

            // 북마크 상태에 따라 아이콘 변경
            val icon = if (isBookmarked) R.drawable.star_yellow else R.drawable.star
            bookmarkButton.setImageResource(icon)
        }
    }


    // ● 뷰 셋
    @OptIn(DelicateCoroutinesApi::class)
    private fun setupViews(view: View) {
        sharedPreferences = requireContext().getSharedPreferences("sp", Context.MODE_PRIVATE)
        dateView = view.findViewById(R.id.fragmentHome_textViewDateNTime)
        temperatureTextView = view.findViewById(R.id.fragmentHomeTop_textViewTemperature)
        tempFeelTextView = view.findViewById(R.id.weatherItem_tempNum)
        humidityTextView = view.findViewById(R.id.weatherItem_humiNum)
        precipitationTextView = view.findViewById(R.id.weatherItem_rainNum)
        windDirectionTextView = view.findViewById(R.id.weatherItem_windDir)
        windSpeedTextView = view.findViewById(R.id.weatherItem_windNum)
        descriptionTextView = view.findViewById(R.id.fragmentHome_textViewTemperatureExplain)
        locationButton = view.findViewById(R.id.fragmentHomeTop_textViewCurrLocation)
        bookmarkButton = view.findViewById(R.id.fragmentHome_imageButtonBookmark)
        searchButton = view.findViewById(R.id.fragmentHome_imageButtonSearch)

        // 날짜 시간 현재로 업데이트
        dateView.text = getCurrentDateTime()

        // 현재 지역 이미지 버튼
        val address = getStringFromPreferences("address")
        sharedWeatherViewModel.updateLocation(address)
        val longAddress = getStringFromPreferences("addressLong")
        Log.d("HTF_JOO", "Address: $address")
        Log.d("HTF_JOO", "AddressLong: $longAddress")

        locationButton.text = address


        // 이미지 버튼 (현재위치로) 클릭하면, 현재 위치정보로 업데이트
        val toCurrentLocation: ImageButton = requireView().findViewById(R.id.toCurrentLocation)
        toCurrentLocation.setOnClickListener {
            getLocationData() // 다시 현재 위치 데이터 가져오기
            val curAdd : String = getStringFromPreferences("address")
            Log.d("BookmarkCheck: 현재위치로", curAdd)
            locationButton.text = curAdd
            val updatedLoc : Location = getLocationNum(curAdd)
            fetchWeatherData(updatedLoc.latitude, updatedLoc.longitude)

            val isCurrentlyBookmarked = sharedWeatherViewModel.isAddressBookmarked(curAdd)
            Log.d("BookmarkCheck: 현재위치로 북마크여부", isCurrentlyBookmarked.toString())
            updateBookmarkIcon(isCurrentlyBookmarked)
        }

        // 지역 버튼 클릭하면, 즐겨찾기 페이지로
        locationButton.setOnClickListener {
            Log.d("JOO", "Location TextView Clicked")
            val bookmarkFragment = BookmarkFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentHomeFrameLayout, bookmarkFragment, "LOCATION_FRAGMENT")
                .addToBackStack(null)
                .commit()
        }


        // 검색 버튼 클릭하면, 도로명 주소 검색 페이지로
        searchButton.setOnClickListener {
            Log.d("JOO", "Search ImageButton Clicked")
            val searchFragment = SearchFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.detach(HomeTopFragment())
            transaction.replace(R.id.fragmentHomeFrameLayout, searchFragment)
                .addToBackStack("SEARCH_FRAGMENT")
                .commit()
        }



        // 북마크 버튼 클릭 시 처리
        bookmarkButton.setOnClickListener {
            var isCurrentlyBookmarked: Boolean = sharedWeatherViewModel.isAddressBookmarked(address)
            if (isCurrentlyBookmarked) {
                sharedWeatherViewModel.updateBookmarkState(address, false)
                updateBookmarkIcon(false)
            } else {
                sharedWeatherViewModel.updateBookmarkState(address, true)
                updateBookmarkIcon(true)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        Log.d("HTF_onResume", "htf resume started.")
    }


    // ● 북마크아이콘
    private fun updateBookmarkIcon(isBookmarked: Boolean) {
        if (isBookmarked) {
            bookmarkButton.setImageResource(R.drawable.star_yellow)
            bookmarkButton.tag = R.drawable.star_yellow
        } else {
            bookmarkButton.setImageResource(R.drawable.star)
            bookmarkButton.tag = R.drawable.star
        }
    }


    // ● sharedPreferences 가져오기
    private fun getStringFromPreferences(key: String, defaultValue: String = ""): String {
        val sharedPreferences = requireContext().getSharedPreferences("sp", Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }


    // ● 주기적 날씨 업데이트
    private fun setupWeatherUpdate() {
        updateRunnable = Runnable {
            latitude = getStringFromPreferences("latitude").toDouble()
            longitude = getStringFromPreferences("longitude").toDouble()
            Log.d("LatJoo", latitude.toString() +","+longitude)
             fetchWeatherData(latitude, longitude)
            updateHandler.postDelayed(updateRunnable, 60000)  // 1분 후에 다시 실행 (테스트: 1초)
        }
        updateHandler.post(updateRunnable)  // 처음 실행
    }

    // ● 시간 주기적 업데이트
    private fun startUpdatingDateTime() {
        val runnable = object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val currentMinute = SimpleDateFormat("mm", Locale.getDefault()).format(currentTime).toInt()

                // 분이 바뀔 때만 TextView 업데이트
                if (currentMinute != lastMinute) {
                    lastMinute = currentMinute
                    dateView.text = getCurrentDateTime()
                }

                // 매초마다 실행
                val nextUpdateTime = 60000 - (currentTime % 60000) // 다음 분까지 남은 밀리초 계산
                updateHandler.postDelayed(this, nextUpdateTime)
            }
        }

        // 첫 실행
        updateHandler.post(runnable)
    }

    // ● 날씨 데이터 받아오기 (패치)
    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val weatherService = RetrofitFactory.getInstance().create(WeatherService::class.java)
        val call = weatherService.getWeather(latitude, longitude, BuildConfig.API_KEY_CURRENT_WEATHER, "metric")

        call.enqueue(object : Callback<WeatherDTO> {
            override fun onResponse(call: Call<WeatherDTO>, response: Response<WeatherDTO>) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    if (weather != null) {
                        sharedWeatherViewModel.updateWeatherData(weather)
                        requireActivity().runOnUiThread {
                            updateWeatherUI(weather)
                            Log.d("JOO_날씨업데이트: ", "$weather")
                        }
                    } else {
                        updateUIError()
                    }
                } else {
                    updateUIError()
                    Log.d("JOO_날씨 업데이트 에러: ", "${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WeatherDTO>, t: Throwable) {
                updateUIError()
            }
        })
    }

    // ● UI 업데이트 ( UI Thread = Main Thread )
    private fun updateWeatherUI(weather: WeatherDTO) {
        var DF = DecimalFormat("0.0") // 소수점 첫째자리까지 항상 표시 (0), 없을 때 표시 안하려면 (#)
        DF.roundingMode = RoundingMode.HALF_EVEN // 값이 가장 가까운 곳으로 반올림
        var rain_sum : String = rain_onehour(weather)

        temperatureTextView.text = DF.format(weather.current.temp)
        tempFeelTextView.text = DF.format(weather.current.feels_like)
        humidityTextView.text = "${weather.current.humidity}"
        precipitationTextView.text = rain_sum
        windDirectionTextView.text = windDirConvert(weather.current.wind_deg)
        windSpeedTextView.text = DF.format(weather.current.wind_speed)
        descriptionTextView.text = weather.current.weather[0].description
    }

    // 한 시간 강수량 데이터
    private fun rain_onehour(weather: WeatherDTO) : String{
        var rain_sum : Double = 0.0
        for (i : Int in 0..59){
            rain_sum += weather.minutely?.get(i)?.precipitation ?: 0.0
        }
        if (rain_sum == 0.0) return "-"
        return rain_sum.toString()
    }

    // ● 데이터 없으면 UI 업데이트
    fun updateUIError() {
        temperatureTextView.text = "N/A"
        tempFeelTextView.text = "N/A"
        humidityTextView.text = "N/A"
        precipitationTextView.text = "N/A"
        windDirectionTextView.text = "N/A"
        windSpeedTextView.text = "N/A"
        descriptionTextView.text = "N/A"
    }

    // ● 각도에 따른 풍향 반환
    private fun windDirConvert(windNum: Int): String {
        return when (windNum.toDouble()) {
            in 0.0..22.5 -> "북"
            in 22.5..67.5 -> "북동"
            in 67.5..112.5 -> "동"
            in 112.5..157.5 -> "남동"
            in 157.5..202.5 -> "남"
            in 202.5..247.5 -> "남서"
            in 247.5..292.5 -> "서"
            in 292.5..337.5 -> "북서"
            in 337.5..360.0 -> "북"
            else -> "N/A" // 잘못된 각도 처리
        }
    }

    // ● 현재시각을 내가 원하는 포맷으로
    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("MM.dd.(EEE) HH:mm", Locale.KOREA)
        return dateFormat.format(System.currentTimeMillis())
    }


    // // ● 프래그먼트 destryoed됐을 때 시간 업데이트 멈춤
    private fun stopUpdatingDateTime() {
        // 핸들러에서 모든 콜백 제거
        updateHandler.removeCallbacksAndMessages(null)
    }

    // ●위치정보 1분마다 가져오기
    private fun startFetchingLocationData() {
        lifecycleScope.launch {
            while (true) {
                getLocationData()
                delay(60000L) // 1분 대기
            }
        }
    }

    // ● 위치정보 가져오기
    private fun getLocationData() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            providerClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    currentAddress = getLocationStr(latitude, longitude)

                    // 위도, 경도, 주소명 저장
                    saveStringInPreferences("latitude_cur", "$latitude")
                    saveStringInPreferences("longitude_cur", "$longitude")
                    saveStringInPreferences("address_cur", addressTrim(currentAddress!!)) // 짧은 주소 저장
                    saveStringInPreferences("addressLong_cur", currentAddress!!.replace("대한민국 ", "")) // 좀 더 긴 주소 저장

                    // 위도, 경도, 주소 Log 찍기
                    Log.d("JOO_HomeTopF_현재위치 가져오기:", "$latitude, $longitude")
                    Log.d("JOO_HomeTopF_현재위치 가져오기:", currentAddress!!)
                    Log.d("JOO_HomeTopF_상세위치 가져오기:", currentAddress!!.replace("대한민국 ", ""))

                    // 날씨 정보 업데이트
                    fetchWeatherData(latitude, longitude)
                } else {
                    Log.d("HomeTopFragment", "Location is null")
                }
            }.addOnFailureListener {
                Log.d("HomeTopFragment", "Failed to get location")
            }
        } else {
            // 권한 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    // ● (경도, 위도) 지오코더로 (주소명)으로
    private fun getLocationStr(lat: Double, lng: Double): String {
        var nowAddr = "현재 위치를 확인 할 수 없습니다."
        val geocoder = Geocoder(requireContext(), Locale.KOREA)
        try {
            val address: List<Address>? = geocoder.getFromLocation(lat, lng, 1)
            if (!address.isNullOrEmpty()) {
                nowAddr = address[0].getAddressLine(0)
            }
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
        return nowAddr
    }

    // ● sharedPreferences에 key:value 저장하는 함수
    private fun saveStringInPreferences(key: String, value: String) {
        val sharedPreferences = requireContext().getSharedPreferences("sp", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(key, value)
            commit()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100
    }

    // ● 대한민국 글자 제거하고 4개 단어만 들고오기
    private fun addressTrim(address: String): String {
        return address
            .replace("대한민국", "") // "대한민국" 제거
            .trim() // 앞뒤 공백 제거
            .split(" ") // 공백으로 분리
            .take(3) // 최대 4개의 요소만 가져옴
            .joinToString(" ") // 다시 공백으로 합침
    }

    // ● 주소로 위도,경도 구하는 GeoCoding
    fun getLocationNum(address: String): Location {
        return try {
            Geocoder(requireActivity(), Locale.KOREA).getFromLocationName(address, 1)?.let{
                Location("").apply {
                    latitude =  it[0].latitude
                    longitude = it[0].longitude
                }
            }?: Location("").apply {
                latitude = 0.0
                longitude = 0.0
            }
        }catch (e:Exception) {
            e.printStackTrace()
            getLocationNum(address) //재시도
        }
    }

    // ● SharedPreferences에서 주소 삭제
    private fun removeFromPreferences(key: String) {
        with(sharedPreferences.edit()) {
            remove(key)
            apply()
        }
    }

    // ● SharedPreferences에서 위치 변경 감지
    private fun observeLocationChanges() {
        sharedWeatherViewModel.currentLocation.observe(viewLifecycleOwner) { newLocation ->
            locationButton.text = newLocation
            val updatedLoc = getLocationNum(newLocation)
            fetchWeatherData(updatedLoc.latitude, updatedLoc.longitude)
        }
    }
}
