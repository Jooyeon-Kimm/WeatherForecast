package com.example.bottomnavigation


import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bottomnavigation.databinding.ActivityMainBinding
import com.example.bottomnavigation.models.SharedWeatherViewModel
import com.example.bottomnavigation.ui.ViewPagerTopBottomAdapter
import com.example.bottomnavigation.ui.home.HomeFragment
import com.example.bottomnavigation.ui.home.HomeTopFragment
import com.example.bottomnavigation.ui.home.RetrofitFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale



class MainActivity : AppCompatActivity() {

    /*** binding & MainActivity ***/
    private lateinit var binding: ActivityMainBinding
    private lateinit var providerClient: FusedLocationProviderClient

    // 위치 권한 요청 코드
    companion object { private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 101 }
    val sharedWeatherViewModel: SharedWeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /***********코드************/
        // 1분마다 사용자 위치 얻기
        // 위치 정보 클라이언트 초기화
        providerClient = LocationServices.getFusedLocationProviderClient(this)
        startSavingLocationData()

        // HomeTopFragment를 초기 화면으로 설정
        if (savedInstanceState == null) { // 기존에 저장된 상태가 없을 때만 실행
            val homeFragment = HomeFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainActivityFragment, homeFragment) // mainActivityFragment에 HomeTopFragment 표시
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE) // 전환 애니메이션(optional)
                .commit()
        }

    }


    private fun startSavingLocationData() {
        lifecycleScope.launch {
            while (true) {
                saveLocationData()
                delay(60000L) // 1분 대기
            }
        }
    }

    // sharedPreferences에 위치정보 저
    private fun saveLocationData() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            providerClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Log.i("latitudeJOO", "$latitude")
                    Log.i("longitudeJOO", "$longitude")

                    val address = getLocationStr(latitude, longitude)

                    // 위도, 경도, 주소명 저장
                    saveStringInPreferences("latitude", "$latitude")
                    saveStringInPreferences("longitude", "$longitude")
                    saveStringInPreferences("address", addressTrim(address)) // string 데이터 저장
                    saveStringInPreferences("addressLong", address.replace("대한민국 ", ""))

                    // 위도, 경도, 주소 Log 찍기
                    Log.d("MainActivity_Location", "$latitude, $longitude") // 받아오는 것 확인함
                    Log.d("MainActivity_Location", address)
                    Log.d("MainActivity_Location", addressTrim(address))
                } else {
                    Log.d("MainActivity", "Location is null")
                }
            }.addOnFailureListener {
                Log.d("MainActivity", "Failed to get location")
            }
        } else {
            // 권한 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    // ■ 주소로 위도,경도 구하는 GeoCoding
    fun getLocationNum(address: String): Location {
        return try {
            Geocoder(this, Locale.KOREA).getFromLocationName(address, 1)?.let{
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

    // ■ 위도 경도로 주소 구하는 Reverse-GeoCoding
    private fun getLocationStr(lat: Double, lng: Double): String {
        var nowAddr = "현재 위치를 확인 할 수 없습니다."
        val geocoder = Geocoder(this, Locale.KOREA)
        val address: List<Address>?

        try {
            address = geocoder.getFromLocation(lat, lng, 1)
            if (!address.isNullOrEmpty()) {
                nowAddr = address[0].getAddressLine(0).toString()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
        return nowAddr
    }

    // ■ SharedPreference 문자열 저장하는 함수
    private fun saveStringInPreferences(key: String, string: String) {
        val sharedPreferences = this.getSharedPreferences("sp", MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(key, string)
        editor.apply()
    }

    // ■ 대한민국 글자 제거하고 4개 단어만 들고오기
    private fun addressTrim(address: String): String {
        return address
            .replace("대한민국", "") // "대한민국" 제거
            .trim() // 앞뒤 공백 제거
            .split(" ") // 공백으로 분리
            .take(3) // 최대 4개의 요소만 가져옴
            .joinToString(" ") // 다시 공백으로 합침
    }

}