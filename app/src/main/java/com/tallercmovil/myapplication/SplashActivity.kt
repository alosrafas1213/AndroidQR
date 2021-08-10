package com.tallercmovil.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        Handler(Looper.myLooper()!!).postDelayed(Runnable(){
            val inicio = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(inicio)
            finish()
        }, 3000)
    }
}