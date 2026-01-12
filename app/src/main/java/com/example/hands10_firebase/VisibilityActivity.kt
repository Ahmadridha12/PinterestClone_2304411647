package com.example.hands10_firebase

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class VisibilityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visibility)

        findViewById<ImageView>(R.id.btnBackVisibility).setOnClickListener {
            finish()
        }
    }
}