package com.example.letschat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    lateinit var toolbar : com.google.android.material.appbar.MaterialToolbar
    lateinit var viewPager: ViewPager2
    lateinit var tabs : com.google.android.material.tabs.TabLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        viewPager = findViewById(R.id.viewPager)
        tabs = findViewById(R.id.tabs)

        setSupportActionBar(toolbar)
        viewPager.adapter = ScreenSliderAdapter(this)

        // this is for tab touch
        TabLayoutMediator(tabs, viewPager,
        TabLayoutMediator.TabConfigurationStrategy{tab: TabLayout.Tab, pos: Int ->
            when(pos){
                0 -> tab.text = "CHATS"
                1 -> tab.text = "PEOPLE"
            }
        }).attach()

    }
}