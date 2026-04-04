package com.ultimatejw.mjcn.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.data.local.MjcnDatabase
import com.ultimatejw.mjcn.data.repository.UserRepository
import com.ultimatejw.mjcn.databinding.ActivityMainBinding
import com.ultimatejw.mjcn.ui.auth.AuthActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostMain) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        val userRepository = UserRepository(this, MjcnDatabase.getInstance(this).userDao())
        binding.btnTempLogout.setOnClickListener {
            lifecycleScope.launch {
                userRepository.logout()
                val intent = Intent(this@MainActivity, AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        // 상세 화면에서는 BottomNav 숨김
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.themeDetailFragment,
                R.id.chatDetailFragment,
                R.id.noticeDetailFragment -> {
                    binding.bottomNav.visibility = android.view.View.GONE
                }
                else -> {
                    binding.bottomNav.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
}
