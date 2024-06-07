package com.rodrigo.deeplarva.routes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity: AppCompatActivity() {
    private val splashScreenTime: Long = 2000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spash_screen)
        GlobalScope.launch {
            delay(splashScreenTime)
            withContext(Dispatchers.Main) {
                var intent = Intent(this@SplashActivity, PicturesActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}