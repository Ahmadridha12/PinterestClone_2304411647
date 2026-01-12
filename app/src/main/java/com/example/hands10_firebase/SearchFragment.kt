package com.example.hands10_firebase

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.util.*

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var adapter: pinAdapter
    private var allPins = mutableListOf<Pin>()
    private val dbRef = FirebaseDatabase.getInstance().getReference("Pins")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvSearch = view.findViewById<RecyclerView>(R.id.rvSearch)
        val searchView = view.findViewById<SearchView>(R.id.searchView)

        // Setup RecyclerView (Pakai Staggered biar mirip Pinterest)
        adapter = pinAdapter(allPins)
        rvSearch.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rvSearch.adapter = adapter

        // Ambil Data dari Firebase
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allPins.clear()
                for (data in snapshot.children) {
                    val pin = data.getValue(Pin::class.java)
                    pin?.let { allPins.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Logika Mengetik di Search Bar
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (newText.isNullOrEmpty()) {
                    allPins
                } else {
                    allPins.filter { it.title?.lowercase(Locale.ROOT)?.contains(newText.lowercase(Locale.ROOT)) == true }
                }
                adapter.filterList(filteredList)
                return true
            }
        })
    }
}