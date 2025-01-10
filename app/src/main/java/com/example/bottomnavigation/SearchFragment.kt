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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.Fragment
import com.example.bottomnavigation.data.WeatherDTO
import com.example.bottomnavigation.databinding.FragmentSearchBinding
import com.example.bottomnavigation.models.WeatherService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale


class SearchFragment : Fragment() {
    private lateinit var binding : FragmentSearchBinding
    private lateinit var toolbar : Toolbar

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

//        requireActivity().runOnUiThread{
//            requireActivity().supportFragmentManager.popBackStack() // HomeTopFragment로 돌아가기
//        }
    }

    inner class BridgeInterface {
        @JavascriptInterface
        fun processDATA(data: String) {
            Log.d("AddressFragment", "Address data received: $data")
            // 필요한 경우 콜백이나 ViewModel을 사용해 데이터 처리
            Toast.makeText(requireContext(), "주소: $data", Toast.LENGTH_SHORT).show()
            Log.d("JOO_AddressAPI", "주소: $data")

            // SharedPreferences에 새로운 위치 저장
            saveStringInPreferences("changedAddress", data)
            saveBooleanInPreferences("isManualAddress", true) //  검색한 위치를 HomeFragment에 반환 여부

            // 프래그먼트 종료
            requireActivity().supportFragmentManager.popBackStack()

            val homeTopLocTV = requireActivity().findViewById<TextView>(R.id.fragmentHomeTop_textViewCurrLocation)
            homeTopLocTV.text = data
            saveStringInPreferences("changedAddress", data)
        }
    }

    // ■ SharedPreference 문자열 저장하는 함수
    private fun saveStringInPreferences(key: String, string: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("sp", MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(key, string)
        editor.apply()
    }

    private fun saveBooleanInPreferences(key: String, boolean: Boolean) {
        val sharedPreferences = requireActivity().getSharedPreferences("sp", MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(key, boolean)
        editor.apply()
    }


}