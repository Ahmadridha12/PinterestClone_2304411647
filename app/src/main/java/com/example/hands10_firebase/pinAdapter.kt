package com.example.hands10_firebase

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class pinAdapter(private var pinList: MutableList<Pin>) : RecyclerView.Adapter<pinAdapter.PinViewHolder>() {

    class PinViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPin: ImageView = view.findViewById(R.id.imgPin)
        val tvTitlePin: TextView = view.findViewById(R.id.tvTitlePin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pin, parent, false)
        return PinViewHolder(view)
    }

    override fun onBindViewHolder(holder: PinViewHolder, position: Int) {
        val currentPin = pinList[position]
        holder.tvTitlePin.text = currentPin.title

        Glide.with(holder.itemView.context)
            .load(currentPin.imageUrl)
            .into(holder.imgPin)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailPinActivity::class.java)
            intent.putExtra("ID", currentPin.id)
            intent.putExtra("TITLE", currentPin.title)
            intent.putExtra("URL", currentPin.imageUrl)
            intent.putExtra("OWNER_ID", currentPin.ownerId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = pinList.size

    // FUNGSI INI YANG HILANG: Tambahkan agar SearchFragment tidak error
    fun filterList(filteredList: List<Pin>) {
        this.pinList = filteredList.toMutableList()
        notifyDataSetChanged()
    }
}