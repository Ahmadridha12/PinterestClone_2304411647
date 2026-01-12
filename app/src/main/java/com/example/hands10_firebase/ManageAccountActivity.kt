package com.example.hands10_firebase

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ManageAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_account)

        val user = FirebaseAuth.getInstance().currentUser
        val tvEmail = findViewById<TextView>(R.id.tvUserEmailManage)
        val btnBack = findViewById<ImageView>(R.id.btnBackManage)

        btnBack.setOnClickListener { finish() }

        // Set email dari Firebase Auth
        user?.let {
            tvEmail.text = it.email
        }
    }
}