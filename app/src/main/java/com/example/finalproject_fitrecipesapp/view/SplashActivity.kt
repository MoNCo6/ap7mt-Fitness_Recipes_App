package com.example.finalproject_fitrecipesapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Nájdeme tlačidlo pomocou jeho ID
        val btnGetStarted: Button = findViewById(R.id.btn_get_started)

        // Nastavenie ClickListener na tlačidlo
        btnGetStarted.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}