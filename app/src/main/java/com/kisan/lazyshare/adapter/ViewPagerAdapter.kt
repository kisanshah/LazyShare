package com.kisan.lazyshare.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kisan.lazyshare.ui.AppFragment
import com.kisan.lazyshare.ui.DocFragment
import com.kisan.lazyshare.ui.ImageFragment
import com.kisan.lazyshare.ui.VideoFragment

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(
        fragmentManager, lifecycle
    ) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ImageFragment()
            1 -> VideoFragment()
            2 -> AppFragment()
            3 -> DocFragment()
            else -> {
                Fragment()
            }
        }
    }
}