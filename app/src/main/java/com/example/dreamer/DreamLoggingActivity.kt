package com.example.dreamer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.dreamer.R
import com.example.dreamer.ui.DreamLoggingFragment

import com.example.dreamer.ui.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class DreamLoggingActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView

    private val profileFragment = ProfileFragment()
    private val dreamLoggingFragment = DreamLoggingFragment()
    private val historyFragment = HistoryFragment()

    private var activeFragment: Fragment = dreamLoggingFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dream_logging)
        bottomNav = findViewById(R.id.bottomNav)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.container, dreamLoggingFragment, "dreamLogging")
            add(R.id.container, profileFragment, "profile").hide(profileFragment)
            add(R.id.container, historyFragment, "history").hide(historyFragment)
            commit()
        }
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> {
                    switchFragment(profileFragment)
                    true
                }
                R.id.navigation_dream -> {
                    switchFragment(dreamLoggingFragment)
                    true
                }
                R.id.navigation_history -> {
                    switchFragment(historyFragment)
                    true
                }
                else -> false
            }
        }

        // Set default selection
        bottomNav.selectedItemId = R.id.navigation_dream
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            hide(activeFragment)
            show(fragment)
            commit()
        }
        activeFragment = fragment
        invalidateOptionsMenu()
    }
}
