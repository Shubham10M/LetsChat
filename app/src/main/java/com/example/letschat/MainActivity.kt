package com.example.letschat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Image_Options.setOnClickListener {
            val popupMenu = android.widget.PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.myProfile -> {
                       val intent = Intent(this, SignupActivity::class.java)
                    startActivity(intent)
                        true
                    }
                    R.id.Setting -> {
//                      Toast.makeText(this, "setting msg showing" , Toast.LENGTH_LONG).show()
                        val intent = Intent(this,PreferenceActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }

            }
            popupMenu.inflate(R.menu.main_menu)
            popupMenu.show()          
        }


        setSupportActionBar(toolbar)
        viewPager.adapter = ScreenSliderAdapter(this)

        // this is for tab touch
        TabLayoutMediator(tabs, viewPager,
                TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, pos: Int ->
                    when (pos) {
                        0 -> tab.text = "CHATS"
                        1 -> tab.text = "PEOPLE"
//                        2 ->tab.text = "TIMELINE"
                    }
                }).attach()

    }
}