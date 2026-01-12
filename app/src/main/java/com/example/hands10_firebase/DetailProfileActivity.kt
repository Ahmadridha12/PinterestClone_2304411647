package com.example.hands10_firebase

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class DetailProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_profile)

        val layoutLihatProfil = findViewById<LinearLayout>(R.id.layoutLihatProfil)
        val btnBack = findViewById<ImageView>(R.id.btnBackProfile)
        val btnLogout = findViewById<TextView>(R.id.btnLogout)

        btnBack.setOnClickListener { finish() }

        // Klik Lihat Profil -> Kirim sinyal IS_VIEW_ONLY ke MainActivity
        layoutLihatProfil.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("TARGET_FRAGMENT", "PROFILE")
            intent.putExtra("IS_VIEW_ONLY", true) // Kunci untuk menyembunyikan Edit & Kolase
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, login_activity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}