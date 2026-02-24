package com.haitao.haimei

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val greetingText = findViewById<TextView>(R.id.greeting_text)
        greetingText.text = getString(R.string.hello_android)
    }
}