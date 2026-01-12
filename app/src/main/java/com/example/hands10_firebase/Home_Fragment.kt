package com.example.hands10_firebase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class Home_Fragment : Fragment() {

    private lateinit var rvHome: RecyclerView
    private lateinit var pinAdapter: pinAdapter
    private val pinList = mutableListOf<Pin>()
    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Pastikan Anda memiliki layout fragment_home.xml dengan RecyclerView di dalamnya
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 1. Inisialisasi RecyclerView
        rvHome = view.findViewById(R.id.rvHome)

        // Menggunakan GridLayoutManager agar tampilan seperti Pinterest (2 kolom)
        rvHome.layoutManager = GridLayoutManager(context, 2)

        pinAdapter = pinAdapter(pinList)
        rvHome.adapter = pinAdapter

        // 2. Ambil Data dari Firebase
        dbRef = FirebaseDatabase.getInstance().getReference("Pins")
        fetchData()

        return view
    }

    private fun fetchData() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pinList.clear()
                if (snapshot.exists()) {
                    for (pinSnapshot in snapshot.children) {
                        val pin = pinSnapshot.getValue(Pin::class.java)
                        pin?.let { pinList.add(it) }
                    }
                    pinAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error jika diperlukan
            }
        })
    }
}