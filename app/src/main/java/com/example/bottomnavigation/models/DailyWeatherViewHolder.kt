package com.example.bottomnavigation.models

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavigation.R
import com.example.bottomnavigation.data.DailyWeatherItem

class DailyWeatherViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun bind(item: DailyWeatherItem) {
        view.findViewById<TextView>(R.id.dateDayofWeek).text = item.dateDayOfWeek
        view.findViewById<TextView>(R.id.dateNum).text = item.dateNum
        view.findViewById<TextView>(R.id.rainAM).text = "${item.rainProbAM}%"
        view.findViewById<TextView>(R.id.rainPM).text = "${item.rainProbPM}%"
        view.findViewById<TextView>(R.id.temp_min).text = "${item.minTemp}°"
        view.findViewById<TextView>(R.id.temp_max).text = "${item.maxTemp}°"
    }
}