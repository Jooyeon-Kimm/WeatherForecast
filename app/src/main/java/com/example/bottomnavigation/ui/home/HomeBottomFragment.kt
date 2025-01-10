package com.example.bottomnavigation.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavigation.R
import com.example.bottomnavigation.data.DailyWeatherAdapter
import com.example.bottomnavigation.data.DailyWeatherItem
import com.example.bottomnavigation.models.SharedWeatherViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeBottomFragment : Fragment() {
    private val sharedWeatherViewModel: SharedWeatherViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var weatherAdapter: DailyWeatherAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_bottom, container, false)
        recyclerView = view.findViewById(R.id.dailyRecyclerViewHomeBottom)
        recyclerView.layoutManager = LinearLayoutManager(context)
        weatherAdapter = DailyWeatherAdapter(emptyList())
        recyclerView.adapter = weatherAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedWeatherViewModel.dailyWeatherItems.observe(viewLifecycleOwner) { items ->
            if (items != null && items.isNotEmpty()) {
                view.findViewById<TextView>(R.id.hourly_forecast_text2_date).text= items[0].dateNum + " ~ " + items[6].dateNum
                weatherAdapter.updateData(items) // 어댑터에 데이터 업데이트
                Log.d("HomeBottomFragment", "Items updated in RecyclerView: ${items.size}")
            } else {
                Log.d("HomeBottomFragment", "Received empty or null items list")
            }
        }
    }


    // 요일 알아오는 함수
    private fun doDayOfWeek(): String {
        val cal: Calendar = Calendar.getInstance()
        var strWeek: String = "미정"
        val nWeek: Int = cal.get(Calendar.DAY_OF_WEEK)

        if (nWeek == 1) {
            strWeek = "일"
        } else if (nWeek == 2) {
            strWeek = "월"
        } else if (nWeek == 3) {
            strWeek = "화"
        } else if (nWeek == 4) {
            strWeek = "수"
        } else if (nWeek == 5) {
            strWeek = "목"
        } else if (nWeek == 6) {
            strWeek = "금"
        } else if (nWeek == 7) {
            strWeek = "토"
        }
        return strWeek
    }




}
