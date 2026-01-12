package com.example.hands10_firebase

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(private val notifList: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotifViewHolder>() {

    class NotifViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvUser: TextView = v.findViewById(R.id.tvNotifUser)
        val tvMessage: TextView = v.findViewById(R.id.tvNotifMessage)
        val tvTime: TextView = v.findViewById(R.id.tvNotifTime)
        val ivPin: ImageView = v.findViewById(R.id.ivNotifPin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotifViewHolder(v)
    }

    override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
        val notif = notifList[position]

        holder.tvUser.text = notif.userName ?: "Seseorang"
        holder.tvMessage.text = notif.message ?: ""

        val timestamp = notif.timestamp ?: 0L
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        holder.tvTime.text = sdf.format(date)

        Glide.with(holder.itemView.context)
            .load(notif.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.ivPin)

        // --- FIX KLIK NAVIGASI ---
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            if (!notif.pinId.isNullOrEmpty()) {
                val intent = Intent(context, DetailPinActivity::class.java).apply {
                    putExtra("ID", notif.pinId)
                    putExtra("URL", notif.imageUrl)
                    // Kita tidak kirim TITLE agar DetailPinActivity mengambil yang terbaru dari DB
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = notifList.size
}