package com.example.bottomnavigation.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavigation.R
import com.example.bottomnavigation.data.DailyWeatherAdapter
import com.example.bottomnavigation.models.SharedWeatherViewModel

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
                weatherAdapter.updateData(items) // 어댑터에 데이터 업데이트
            }
        }


    }







}