package com.example.bottomnavigation.models

import android.annotation.SuppressLint
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

// 일간예보 아이템 뷰홀더

// 뷰홀더: 화면에 표시될 데이터나 아이템들을 저장하는 역할
// RecyclerView의 개념을 적용하기위해선 스크롤 해서 위로 올라간 View를 재활용하기 위해서
// 이 View를 기억하고 있어야 합니다.
// ViewHolder가 그역할을 합니다.
class DailyWeatherViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    @SuppressLint("SetTextI18n")
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