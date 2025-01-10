package com.example.bottomnavigation.data

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavigation.R
import android.view.LayoutInflater
import com.example.bottomnavigation.models.DailyWeatherViewHolder


class DailyWeatherAdapter(private var items: List<DailyWeatherItem> = listOf()) : RecyclerView.Adapter<DailyWeatherViewHolder>() {

    // 데이터 업데이트 함수
    fun updateData(newItems: List<DailyWeatherItem>) {
        items = newItems
        notifyDataSetChanged()  // 리스트가 갱신되었음을 알리고 UI를 업데이트합니다.
    }

    // 뷰홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyWeatherViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_daily_weather, parent, false)
        return DailyWeatherViewHolder(itemView)
    }

    // 데이터 바인딩
    override fun onBindViewHolder(holder: DailyWeatherViewHolder, position: Int) {
        val currentItem = items[position]
        holder.bind(currentItem)
    }

    // 아이템 수 반환
    override fun getItemCount() = items.size
}
