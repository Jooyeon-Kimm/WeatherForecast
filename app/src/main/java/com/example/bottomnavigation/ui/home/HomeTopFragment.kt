package com.example.bottomnavigation.ui.home

import FavoriteAddressAdapter
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
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavigation.BuildConfig
import com.example.bottomnavigation.LocationFragment
import com.example.bottomnavigation.R
import com.example.bottomnavigation.SearchFragment
import com.example.bottomnavigation.data.FavoriteAddress
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
    private lateinit var adapter: FavoriteAddressAdapter

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
        Log.d("LC_HTF", "onCreateView: HomeTopFragment")
        return inflater.inflate(R.layout.fragment_home_top, container, false)
    }

    // ■
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        Log.d("LC_HTF", "onViewCreated: HomeTopFragment")

        // 위치 > 날씨 > 시간 업데이트
        providerClient = LocationServices.getFusedLocationProviderClient(requireContext())
        getLocationData()
        startUpdatingDateTime()
        observeLocationChanges()
    }


    // ViewPager로 HomeBottomFragment 갔다가
    // 다시 HomeTopFragmet 가면, onPause()였다가 onResume()인 듯
    override fun onResume() {
        super.onResume()
        Log.d("LC_HTF", "onResume: HomeTopFragment")

    }

    // ■
    override fun onPause() {
        super.onPause()
        Log.d("LC_HTF", "onPause: HomeTopFragment")
        // Fragment가 보이지 않을 때 갱신 중단
        // updateHandler.removeCallbacks(updateRunnable)
    }

    // ■
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("LC_HTF", "onDestroyView: HomeTopFragment")

        // 뷰가 파괴될 때 업데이트 중단
        stopUpdatingDateTime()
    }



    // ● 뷰 셋
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

        // 현재 지역 글자 & 북마크 업데이트 (클릭가능)
        requireActivity().runOnUiThread {
            locationButton.text = sharedWeatherViewModel.currentLocation.value.toString()
        }

        // 이미지 버튼 (현재위치로) 클릭하면, 현재 위치정보로 업데이트
        val toCurrentLocation: ImageButton = requireView().findViewById(R.id.toCurrentLocation)
        toCurrentLocation.setOnClickListener {
            getLocationData() // 다시 현재 위치 데이터 가져오기
            val curLocation = sharedWeatherViewModel.currentLocation.value.toString()
            Log.d("BookmarkCheck: 현재위치로", curLocation)
            locationButton.text = curLocation
            val updatedLoc : Location = getLocationNum(curLocation)
            Log.d("LocationUpdate", "$updatedLoc : $curLocation : ${updatedLoc.latitude} : ${updatedLoc.longitude}")
            fetchWeatherData(updatedLoc.latitude, updatedLoc.longitude)

            val isBookmarked = sharedWeatherViewModel.isAddressBookmarked(curLocation)
            updateBookmarkIcon(isBookmarked)
        }

        // 지역 버튼 클릭하면, 즐겨찾기 페이지로
        locationButton.setOnClickListener {
            Log.d("JOO", "Location TextView Clicked")
            val locationFragment = LocationFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentHomeFrameLayout, locationFragment, "LOCATION_FRAGMENT")
                .addToBackStack(null)  // 뒤로 가기 스택
                .commit()
        }


        // 검색 버튼 클릭하면, 도로명 주소 검색 페이지로
        searchButton.setOnClickListener {
            val searchFragment = SearchFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            val currentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentHomeFrameLayout)
            currentFragment?.let {
                transaction.hide(it)
            }
            transaction.replace(R.id.fragmentHomeFrameLayout, searchFragment)
                .addToBackStack("SEARCH_FRAGMENT")
                .commit()
        }



        // 북마크 버튼 클릭 시 처리
        bookmarkButton.setOnClickListener {
            val address = sharedWeatherViewModel.currentLocation.value.toString()
            if (address.isEmpty()) {
                Log.e("LC_BookmarkCheck", "Address is null or empty. Cannot update bookmark state.")
                return@setOnClickListener
            }

            val isCurrentlyBookmarked = sharedWeatherViewModel.isAddressBookmarked(address)
            Log.d("LC_BookmarkCheckHTF", "add: $address, bm: $isCurrentlyBookmarked")
            if (isCurrentlyBookmarked) {
                sharedWeatherViewModel.updateBookmarkState(address, false)
                updateBookmarkIcon(false)
                Log.d("LC_BookmarkCheck", "$address is now unbookmarked.")
                fetchBookmarkData(address)
            } else {
                sharedWeatherViewModel.updateBookmarkState(address, true)
                updateBookmarkIcon(true)
                Log.d("LC_BookmarkCheck", "$address is now bookmarked.")
                fetchBookmarkData(address)

            }

            Log.d("LC_BookmarkCheck", "############################################################")
            sharedWeatherViewModel.addresses.value?.forEach {
                Log.d("LC_BookmarkCheck", "Address: ${it.title}, Bookmarked: ${it.isBookmarked}")
            }
        }


    }


    // ● 북마크아이콘
    private fun updateBookmarkIcon(isBookmarked: Boolean) {
        requireActivity().runOnUiThread {
            if (isBookmarked) {
                bookmarkButton.setImageResource(R.drawable.star_yellow)
                bookmarkButton.tag = R.drawable.star_yellow
            } else {
                bookmarkButton.setImageResource(R.drawable.star)
                bookmarkButton.tag = R.drawable.star
            }
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
            latitude = sharedWeatherViewModel.currentLatitude.value?.toDouble() ?: 0.0
            longitude = sharedWeatherViewModel.currentLatitude.value?.toDouble() ?: 0.0
            Log.d("LatJoo", latitude.toString() +","+longitude)
            fetchWeatherData(latitude, longitude)
//            updateHandler.postDelayed(updateRunnable, 60000)  // 1분 후에 다시 실행 (테스트: 1초)
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

                            Log.d("LC_JOO_날씨업데이트: ", "$weather")
                        }
                    } else {
                        updateUIError()
                    }
                } else {
                    updateUIError()
                    Log.d("LC_JOO_날씨 업데이트 에러: ", "${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WeatherDTO>, t: Throwable) {
                updateUIError()
            }
        })
    }

    // ● UI 업데이트 ( UI Thread = Main Thread )
    private fun updateWeatherUI(weather: WeatherDTO) {
        val df = DecimalFormat("0.0") // 소수점 첫째자리까지 (항상 존재: 0, 자릿수 없어도 되면 #)
        df.roundingMode = RoundingMode.HALF_EVEN // 값이 가장 가까운 곳으로 반올림

        requireActivity().runOnUiThread {
            temperatureTextView.text = df.format(weather.current.temp).toString()
            tempFeelTextView.text = df.format(weather.current.feels_like)
            humidityTextView.text = "${weather.current.humidity}"

            // 1시간 동안 강수량 모두 더하기 (분 데이터 합계)
            var rainSum = 0.0
            for(i:Int in 0..59){
                rainSum += weather.minutely?.get(i)?.precipitation ?: 0.0

                if (rainSum != 0.0) precipitationTextView.text = rainSum.toString()
                else precipitationTextView.text = "-"
            }
            windDirectionTextView.text = windDirConvert(weather.current.wind_deg)
            windSpeedTextView.text = df.format(weather.current.wind_speed)
            descriptionTextView.text = weather.current.weather[0].description
        }
    }

    // ● 데이터 없으면 UI 업데이트
    fun updateUIError() {
        requireActivity().runOnUiThread {
            temperatureTextView.text = "N/A"
            tempFeelTextView.text = "N/A"
            humidityTextView.text = "N/A"
            precipitationTextView.text = "N/A"
            windDirectionTextView.text = "N/A"
            windSpeedTextView.text = "N/A"
            descriptionTextView.text = "N/A"
        }
    }

    private fun fetchBookmarkData(currentAddress : String){
        val isBookmarked = sharedWeatherViewModel.isAddressBookmarked(currentAddress)
        Log.d("JOONewLocation_fetchbm", "$isBookmarked, $currentAddress")
        if(isBookmarked){
            updateBookmarkIcon(true)
        }else{
            updateBookmarkIcon(false)
        }
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
//                    saveStringInPreferences("latitude", "$latitude")
//                    saveStringInPreferences("longitude", "$longitude")
//                    saveStringInPreferences("address", addressTrim(currentAddress!!)) // 짧은 주소 저장
//                    saveStringInPreferences("addressLong", currentAddress!!.replace("대한민국 ", "")) // 좀 더 긴 주소 저장

                    sharedWeatherViewModel.currentLatitude.value = latitude
                    sharedWeatherViewModel.currentLongitude.value = longitude
                    sharedWeatherViewModel.currentLocation.value = addressTrim(currentAddress!!)

                    // 위도, 경도, 주소 Log 찍기
                    Log.d("JOO_HomeTopF_현재위치 가져오기:", "$latitude, $longitude")
                    Log.d("JOO_HomeTopF_현재위치 가져오기:", currentAddress!!)
                    Log.d("JOO_HomeTopF_상세위치 가져오기:", currentAddress!!.replace("대한민국 ", ""))

                    // 날씨 정보 업데이트
                    fetchWeatherData(latitude, longitude)

                    // 북마크 버튼 업데이트
                    fetchBookmarkData(addressTrim(currentAddress!!))
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

    // ● 대한민국 글자 제거하고 3개 단어만 들고오기
    private fun addressTrim(address: String): String {
        return address
            .replace("대한민국", "") // "대한민국" 제거
            .trim() // 앞뒤 공백 제거
            .split(" ") // 공백으로 분리
            .take(3) // 최대 3개의 요소만 가져옴
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

    // ● SharedViewModel에서 위치 변경 감지
    private fun observeLocationChanges() {
        sharedWeatherViewModel.currentLocation.observe(viewLifecycleOwner) { newLocation ->
            locationButton.text = addressTrim(newLocation)
            val updatedLoc = getLocationNum(addressTrim(newLocation))
            Log.d("JOONewLocation", "${addressTrim(newLocation)}: $updatedLoc")
            fetchWeatherData(updatedLoc.latitude, updatedLoc.longitude)
            fetchBookmarkData(addressTrim(newLocation))

            sharedWeatherViewModel.addresses.observe(viewLifecycleOwner){favAddList ->
                fetchBookmarkData(newLocation)
            }
        }

    }




}
