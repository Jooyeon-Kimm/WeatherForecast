package com.example.bottomnavigation

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.bottomnavigation.data.WeatherDTO
import com.example.bottomnavigation.databinding.FragmentSearchBinding
import com.example.bottomnavigation.models.SharedWeatherViewModel
import com.example.bottomnavigation.models.WeatherService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale


class SearchFragment : Fragment() {
    private lateinit var binding : FragmentSearchBinding
    private val sharedWeatherViewModel: SharedWeatherViewModel by activityViewModels()

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LC_SF", "onCreate: SearchFragment")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("LC_SF", "onCreateView: SearchFragment")
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("LC_SF", "onViewCreated: SearchFragment")

        setupWebView()
        val toolbar_close : ImageView = binding.toolbarClose
        toolbar_close.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {

        val webView: WebView = binding.webView
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(BridgeInterface(), "Android")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Javascript 함수 호출
                webView.loadUrl("javascript:sample2_execDaumPostcode();")

                // 웹뷰: 캐시 적용 (시간 단축)
                webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK // 이전에 로드된 페이지를 다시 로드하지 않아도 됨
            }
        }
        // 개인 Host Domain URL 로드
        webView.loadUrl("https://weatherapp-6fa09.web.app/")
    }

    inner class BridgeInterface {
        @JavascriptInterface
        fun processDATA(data: String) {
            Toast.makeText(requireContext(), "주소: $data", Toast.LENGTH_SHORT).show()

            // HomeTop프래그먼트의 위치 textview.text 업데이트
            requireActivity().runOnUiThread {
                val loc: Location = getLocationNum(data)
                sharedWeatherViewModel.currentLatitude.value = loc.latitude
                sharedWeatherViewModel.currentLongitude.value = loc.longitude
                sharedWeatherViewModel.currentLocation.value = addressTrim(data)
            }

            // 프래그먼트 종료
            requireActivity().supportFragmentManager.popBackStack()


        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }



    // ■ 주소로 위도,경도 구하는 GeoCoding
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

    // ● 대한민국 글자 제거하고 3개 단어만 들고오기
    private fun addressTrim(address: String): String {
        return address
            .replace("대한민국", "") // "대한민국" 제거
            .trim() // 앞뒤 공백 제거
            .split(" ") // 공백으로 분리
            .take(3) // 최대 3개의 요소만 가져옴
            .joinToString(" ") // 다시 공백으로 합침
    }


}