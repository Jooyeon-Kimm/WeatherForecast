package com.example.bottomnavigation.ui

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bottomnavigation.ui.home.HomeBottomFragment
import com.example.bottomnavigation.ui.home.Hour1Fragment
import com.example.bottomnavigation.ui.home.Hour3Fragment

class TabFragmentAdapter(fa: HomeBottomFragment) : FragmentStateAdapter(fa) {
    private val NUM_PAGES = 2  // 두 개의 페이지

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Hour1Fragment()  // 1시간 간격
            1 -> Hour3Fragment()  // 3시간 간격
            else -> throw IllegalStateException("Unexpected position $position")

        }
    }

    override fun getItemCount(): Int = NUM_PAGES
}
