package com.example.hands10_firebase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddPin_activity : AppCompatActivity() {

    private val dbRef = FirebaseDatabase.getInstance().getReference("Pins")
    private lateinit var auth: FirebaseAuth // Tambahkan inisialisasi FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datadiri)

        auth = FirebaseAuth.getInstance() // Inisialisasi Auth

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etUrl = findViewById<EditText>(R.id.etImageUrl)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val isEdit = intent.getBooleanExtra("IS_EDIT", false)
        val existingId = intent.getStringExtra("ID")

        if (isEdit) {
            etTitle.setText(intent.getStringExtra("TITLE"))
            etUrl.setText(intent.getStringExtra("URL"))
            btnSave.text = "Update Pin"
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val url = etUrl.text.toString().trim()
            // Ambil UID user yang sedang login saat ini
            val currentUserId = auth.currentUser?.uid

            if (title.isNotEmpty() && url.isNotEmpty()) {
                if (currentUserId != null) {
                    val pinId = if (isEdit) existingId!! else dbRef.push().key ?: System.currentTimeMillis().toString()

                    // Teruskan currentUserId ke fungsi simpan
                    saveToDatabase(pinId, title, url, currentUserId)
                } else {
                    Toast.makeText(this, "Sesi berakhir, silakan login ulang", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Harap isi Judul dan URL Gambar!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Tambahkan parameter ownerId di sini
    private fun saveToDatabase(id: String, title: String, url: String, ownerId: String) {
        // Update: Data class Pin sekarang harus menyertakan ownerId
        val pinData = Pin(id, title, url, ownerId)

        dbRef.child(id).setValue(pinData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showNotification("Pinterest Clone", "Pin '$title' berhasil disimpan!")
                Toast.makeText(this, "Berhasil!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Gagal simpan: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showNotification(title: String, msg: String) {
        val channelId = "pinterest_notif"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Pinterest Notification", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        nm.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}