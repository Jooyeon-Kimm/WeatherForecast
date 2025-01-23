package com.example.bottomnavigation.models

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavigation.R
import com.example.bottomnavigation.data.DailyWeatherItem

class DailyWeatherViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun bind(item: DailyWeatherItem) {
        val dayOfWeekTextView = view.findViewById<TextView>(R.id.dateDayofWeek)
        val dayOfWeekText = item.dateDayOfWeek

        // "(오늘)" 포함하고 있으면, "(오늘)" 글자 크기 0.6배 + 회색으로 변경
        if (dayOfWeekText.contains("(오늘)")) {
            val spannable = SpannableString(dayOfWeekText)
            val startIndex = dayOfWeekText.indexOf("(오늘)")
            val endIndex = startIndex + "(오늘)".length

            // Apply a RelativeSizeSpan to make "(오늘)" smaller
            spannable.setSpan(ForegroundColorSpan(Color.GRAY), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(RelativeSizeSpan(0.6f), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            dayOfWeekTextView.text = spannable
        } else {
            dayOfWeekTextView.text = dayOfWeekText
        }


        view.findViewById<TextView>(R.id.dateNum).text = item.dateNum
        view.findViewById<TextView>(R.id.rainAM).text = "${item.rainProbAM}%"
        view.findViewById<TextView>(R.id.rainPM).text = "${item.rainProbPM}%"
        view.findViewById<TextView>(R.id.temp_min).text = "${item.minTemp}°"
        view.findViewById<TextView>(R.id.temp_max).text = "${item.maxTemp}°"
    }
}