package com.example.hands10_firebase

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MessageFragment : Fragment(R.layout.fragment_message) {

    private lateinit var rvMessage: RecyclerView
    private lateinit var dbRef: DatabaseReference
    private lateinit var notifList: MutableList<Notification> // Gunakan MutableList agar lebih fleksibel
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid ?: ""

        // 1. Inisialisasi RecyclerView
        rvMessage = view.findViewById(R.id.rvMessage)

        // Optimasi LayoutManager agar loading data lebih mulus
        val layoutManager = LinearLayoutManager(context)
        rvMessage.layoutManager = layoutManager

        notifList = mutableListOf()
        notificationAdapter = NotificationAdapter(notifList)
        rvMessage.adapter = notificationAdapter

        if (currentUserId.isEmpty()) return

        // 2. Ambil data dari Firebase
        dbRef = FirebaseDatabase.getInstance().getReference("Notifications").child(currentUserId)

        // Gunakan limitToLast(50) jika notifikasi sudah terlalu banyak agar tidak berat
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notifList.clear()

                if (snapshot.exists()) {
                    for (notifSnap in snapshot.children) {
                        val data = notifSnap.getValue(Notification::class.java)
                        data?.let { notifList.add(it) }
                    }

                    // 3. URUTKAN: Data terbaru (timestamp terbesar) di posisi paling atas
                    notifList.sortByDescending { it.timestamp }
                }

                // 4. Update Adapter
                notificationAdapter.notifyDataSetChanged()

                // Opsional: Scroll otomatis ke paling atas jika ada pesan masuk baru
                if (notifList.isNotEmpty()) {
                    rvMessage.scrollToPosition(0)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Tambahkan log atau pesan jika gagal mengambil data
            }
        })
    }
}