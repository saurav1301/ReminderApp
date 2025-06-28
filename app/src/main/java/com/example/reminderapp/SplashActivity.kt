package com.example.reminderapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    // Duration of splash screen (in milliseconds)
    private val splashDuration: Long = 2000L  // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delayed navigation to MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, AboutActivity::class.java)
            startActivity(intent)
            finish()  // closes splash so user can't go back to it
        }, splashDuration)
    }
}
