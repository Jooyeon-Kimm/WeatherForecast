package com.example.bottomnavigation.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.bottomnavigation.R
import com.example.bottomnavigation.SearchFragment
import com.example.bottomnavigation.ui.ViewPagerTopBottomAdapter

class HomeFragment : Fragment() {
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager = view.findViewById(R.id.viewPagerHomeTop)
        viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = ViewPagerTopBottomAdapter(this)
        viewPager.adapter = adapter
    }


}





// 온도 단위가 F(화씨)가 아니라 K(켈빈) 이었다.
// Frequently Asked Questions - OpenWeatherMap : Kelvin is used by default
// 섭씨로 받고 싶으면: units=metric