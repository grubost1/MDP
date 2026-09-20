package com.example.beetles

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val viewPager = findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(AuthorsFragment(), "Авторы")
        adapter.addFragment(RegistrationFragment(), "Регистрация")
        adapter.addFragment(HomeFragment(), "Жук")
        adapter.addFragment(SettingsFragment(), "Настройки")
        adapter.addFragment(RulesFragment(), "Правила")

        viewPager.adapter = adapter

        val tabIcons = arrayOf(
            R.drawable.ic_authors,
            R.drawable.ic_registration,
            R.drawable.ic_bug,
            R.drawable.ic_settings,
            R.drawable.ic_rules
        )

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(tabIcons[position])
        }.attach()

        viewPager.setCurrentItem(2, false)
    }
}