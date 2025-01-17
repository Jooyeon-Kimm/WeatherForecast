package com.example.bottomnavigation.ui.home

import android.annotation.SuppressLint
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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedWeatherViewModel.dailyWeatherItems.observe(viewLifecycleOwner) { items ->
            if (items != null && items.isNotEmpty()) {
                view.findViewById<TextView>(R.id.hourly_forecast_text2_date).text= items[0].dateNum + " ~ " + items[6].dateNum
                weatherAdapter.updateData(items) // 어댑터에 데이터 업데이트
                Log.d("HomeBottomFragmentJOO", "Items updated in RecyclerView: ${items.size}")
//                for (i:Int in 1..7){
//                    Log.d("itemJOO$i", "${items[i].iconAM}")
//                }
            } else {
                Log.d("HomeBottomFragmentJOO", "Received empty or null items list")
            }
        }
    }







}
