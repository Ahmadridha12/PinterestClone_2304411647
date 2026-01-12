package com.example.hands10_firebase

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var dbRef: DatabaseReference
    private val savedPinList = mutableListOf<Map<String, String>>()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbRef = FirebaseDatabase.getInstance().reference

        // 1. Inisialisasi View Identitas (Hanya untuk Tampilan, tidak diberi klik)
        val imgProfile = view.findViewById<ImageView>(R.id.imgProfile)
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)

        // 2. Inisialisasi Tombol Menu yang Aktif
        val btnManage = view.findViewById<MaterialButton>(R.id.btnManageAccount)
        val btnVis = view.findViewById<MaterialButton>(R.id.btnVisibility)
        val btnNotif = view.findViewById<MaterialButton>(R.id.btnNotifications)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        val rvSavedPins = view.findViewById<RecyclerView>(R.id.rvSavedPins)

        // Update Tampilan Profil (Data User)
        currentUser?.let { user ->
            tvUserName.text = user.displayName ?: "User"
            Glide.with(this).load(user.photoUrl).circleCrop().into(imgProfile)

            // Aktifkan pendengar pop-up notifikasi
            startNotificationListener(user.uid)
        }

        // 3. Setup Klik Menu Pengaturan
        btnManage.setOnClickListener {
            startActivity(Intent(requireContext(), ManageAccountActivity::class.java))
        }

        btnVis.setOnClickListener {
            startActivity(Intent(requireContext(), VisibilityActivity::class.java))
        }

        btnNotif.setOnClickListener {
            // Berpindah ke fragment pesan/notifikasi
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MessageFragment())
                .addToBackStack(null)
                .commit()
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), login_activity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        // 4. Setup RecyclerView untuk Pin yang Disimpan
        rvSavedPins.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_pin, parent, false)
                return object : RecyclerView.ViewHolder(v) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val item = savedPinList[position]
                val ivPin = holder.itemView.findViewById<ImageView>(R.id.imgPin)
                val tvTitle = holder.itemView.findViewById<TextView>(R.id.tvTitlePin)

                tvTitle.text = item["title"]
                Glide.with(holder.itemView.context).load(item["url"]).into(ivPin)

                // Klik Pin untuk lihat detail
                holder.itemView.setOnClickListener {
                    val intent = Intent(requireContext(), DetailPinActivity::class.java).apply {
                        putExtra("ID", item["id"])
                        putExtra("URL", item["url"])
                        putExtra("TITLE", item["title"])
                    }
                    startActivity(intent)
                }
            }
            override fun getItemCount() = savedPinList.size
        }
        rvSavedPins.adapter = adapter

        // Load data Pin Tersimpan dari Firebase
        currentUser?.let { u ->
            dbRef.child("SavedPins").child(u.uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    savedPinList.clear()
                    for (data in snapshot.children) {
                        val pin = data.value as? Map<String, String>
                        pin?.let { savedPinList.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // Fungsi Pop-up Notifikasi Real-time
    private fun startNotificationListener(uid: String) {
        dbRef.child("Notifications").child(uid).limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                private var isFirst = true
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isFirst) { isFirst = false; return }
                    if (snapshot.exists() && isAdded) {
                        for (data in snapshot.children) {
                            val msg = data.child("message").value.toString()
                            activity?.let {
                                android.app.AlertDialog.Builder(it)
                                    .setTitle("Notifikasi")
                                    .setMessage(msg)
                                    .setPositiveButton("OK", null)
                                    .show()
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}