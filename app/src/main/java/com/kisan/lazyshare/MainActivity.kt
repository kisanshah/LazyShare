package com.kisan.lazyshare

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayoutMediator
import com.kisan.lazyshare.adapter.ViewPagerAdapter
import com.kisan.lazyshare.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpViewPager()
    }

    private fun setUpViewPager() {
        binding.apply {
            viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
            viewPager.adapter = viewPagerAdapter

            TabLayoutMediator(categoriesTabLayout, viewPager) { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = "Images"
                    }
                    1 -> tab.text = "Video"
                    2 -> tab.text = "App"
                    3 -> tab.text = "Documents"
                }
            }.attach()
        }
    }
}