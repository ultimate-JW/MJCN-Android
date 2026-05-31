package com.ultimatejw.mjcn.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.ActivityMainBinding
import com.ultimatejw.mjcn.ui.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_main) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        // 탭 전환 시 설정 화면이 백스택에 남지 않도록 먼저 pop
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (navController.currentDestination?.id == R.id.settingsFragment) {
                navController.popBackStack()
            }
            NavigationUI.onNavDestinationSelected(item, navController)
        }

        lifecycleScope.launch {
            viewModel.sessionExpiredFlow.collect {
                val intent = Intent(this@MainActivity, AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.themeDetailFragment,
                R.id.chatDetailFragment,
                R.id.noticeDetailFragment,
                R.id.noticeBookmarkFragment,
                R.id.infoBookmarkFragment -> {
                    binding.bottomNav.visibility = View.GONE
                    binding.bottomNav.menu.setGroupCheckable(0, true, true)
                }
                R.id.settingsFragment -> {
                    binding.bottomNav.visibility = View.VISIBLE
                    // 설정 화면에서는 하단 탭 포커스 해제
                    binding.bottomNav.menu.setGroupCheckable(0, false, true)
                }
                else -> {
                    binding.bottomNav.visibility = View.VISIBLE
                    binding.bottomNav.menu.setGroupCheckable(0, true, true)
                }
            }
        }
    }
}
