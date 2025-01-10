package com.example.bottomnavigation.ui

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bottomnavigation.ui.home.HomeBottomFragment
import com.example.bottomnavigation.ui.home.HomeFragment
import com.example.bottomnavigation.ui.home.HomeTopFragment

class ViewPagerTopBottomAdapter(fa: HomeFragment) : FragmentStateAdapter(fa) {
    private val NUM_PAGES = 2  // 두 개의 페이지

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeTopFragment()  // 상단 프래그먼트
            1 -> HomeBottomFragment()  // 하단 프래그먼트
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }

    override fun getItemCount(): Int = NUM_PAGES

}
