package com.ultimatejw.mjcn.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ultimatejw.mjcn.data.local.MjcnDatabase
import com.ultimatejw.mjcn.data.repository.UserRepository
import com.ultimatejw.mjcn.databinding.ActivitySplashBinding
import com.ultimatejw.mjcn.ui.auth.AuthActivity
import com.ultimatejw.mjcn.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = MjcnDatabase.getInstance(this)
        val userRepository = UserRepository(this, db.userDao())

        lifecycleScope.launch {
            delay(1500)
            val isLoggedIn = userRepository.isLoggedIn.first()
            val intent = if (isLoggedIn) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, AuthActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }
}
