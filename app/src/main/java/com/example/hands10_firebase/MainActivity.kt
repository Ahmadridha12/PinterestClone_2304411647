package com.example.hands10_firebase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_navigation)

        // 1. Minta Izin Notifikasi (Wajib untuk Android 13+)
        checkNotificationPermission()

        // 2. Jalankan Listener
        setupNotificationListener()

        handleIntent(intent)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { loadFragment(Home_Fragment()); true }
                R.id.nav_search -> { loadFragment(SearchFragment()); true }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddPin_activity::class.java))
                    false
                }
                R.id.nav_message -> { loadFragment(MessageFragment()); true }
                R.id.nav_profile -> {
                    val fragment = ProfileFragment()
                    val bundle = Bundle().apply { putBoolean("IS_VIEW_ONLY", false) }
                    fragment.arguments = bundle
                    loadFragment(fragment)
                    true
                }
                else -> false
            }
        }
    }

    // Fungsi untuk meminta izin notifikasi di Android 13+
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun setupNotificationListener() {
        if (currentUser == null) return

        val dbRef = FirebaseDatabase.getInstance().getReference("Notifications").child(currentUser.uid)

        dbRef.limitToLast(1).addValueEventListener(object : ValueEventListener {
            private var isFirstLoad = true

            override fun onDataChange(snapshot: DataSnapshot) {
                if (isFirstLoad) {
                    isFirstLoad = false
                    return
                }

                if (snapshot.exists()) {
                    for (notifSnap in snapshot.children) {
                        val userName = notifSnap.child("userName").value.toString()
                        val message = notifSnap.child("message").value.toString()

                        triggerSystemNotification(userName, message)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun triggerSystemNotification(title: String, message: String) {
        val channelId = "interaction_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Notifikasi Interaksi",
                NotificationManager.IMPORTANCE_HIGH // Wajib HIGH agar muncul melayang
            ).apply {
                description = "Notifikasi aktivitas user"
                enableLights(true)
                enableVibration(true)
                // Memberitahu sistem agar memunculkan banner melayang
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("TARGET_FRAGMENT", "MESSAGES")
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            // Priority High + Defaults All wajib untuk banner heads-up
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Anggap sebagai pesan masuk
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    private fun handleIntent(intent: Intent?) {
        val target = intent?.getStringExtra("TARGET_FRAGMENT")
        if (target == "MESSAGES") {
            bottomNav.selectedItemId = R.id.nav_message
            loadFragment(MessageFragment())
        } else if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            bottomNav.selectedItemId = R.id.nav_home
            loadFragment(Home_Fragment())
        }
    }
}