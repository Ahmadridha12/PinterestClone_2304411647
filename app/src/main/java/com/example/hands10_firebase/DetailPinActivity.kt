package com.example.hands10_firebase

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetailPinActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val commentList = mutableListOf<Map<String, String>>()
    private lateinit var commentAdapter: CommentAdapter
    private var ownerUid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_pin)

        dbRef = FirebaseDatabase.getInstance().reference

        val pinId = intent.getStringExtra("ID") ?: ""
        val pinUrl = intent.getStringExtra("URL") ?: ""
        val pinTitle = intent.getStringExtra("TITLE") ?: ""

        val imgDetail = findViewById<ImageView>(R.id.ivDetailImage)
        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val btnBack = findViewById<ImageView>(R.id.btnBackDetail)
        val btnMore = findViewById<ImageView>(R.id.btnMoreOptions)
        val btnLike = findViewById<ImageView>(R.id.btnLike)
        val tvLikeCount = findViewById<TextView>(R.id.tvLikeCount)
        val tvCommentCount = findViewById<TextView>(R.id.tvCommentCount)
        val btnShare = findViewById<ImageView>(R.id.btnShare)
        val btnSave = findViewById<Button>(R.id.btnSavePin)
        val etComment = findViewById<EditText>(R.id.etComment)
        val btnSendComment = findViewById<ImageView>(R.id.btnSendComment)
        val rvComments = findViewById<RecyclerView>(R.id.rvComments)

        tvTitle.text = pinTitle
        Glide.with(this).load(pinUrl).into(imgDetail)

        // 1. Ambil Data Owner dari Firebase
        dbRef.child("Pins").child(pinId).child("ownerId").get().addOnSuccessListener {
            ownerUid = it.value.toString()
        }

        btnBack.setOnClickListener { finish() }

        // 2. Menu Titik Tiga
        btnMore.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("Download Gambar")
            if (currentUser?.uid == ownerUid) {
                popup.menu.add("Edit Pin")
                popup.menu.add("Hapus Pin")
            }
            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Download Gambar" -> downloadImage(pinUrl)
                    "Edit Pin" -> {
                        val intent = Intent(this, EditPinActivity::class.java)
                        intent.putExtra("ID", pinId)
                        startActivity(intent)
                    }
                    "Hapus Pin" -> {
                        dbRef.child("Pins").child(pinId).removeValue().addOnSuccessListener { finish() }
                    }
                }
                true
            }
            popup.show()
        }

        // 3. Fitur Share
        btnShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Lihat Pin ini: $pinUrl")
            }
            startActivity(Intent.createChooser(intent, "Bagikan lewat"))

            // Notifikasi Share ke pemilik Pin
            if (ownerUid.isNotEmpty() && ownerUid != currentUser?.uid) {
                sendNotification(ownerUid, "membagikan Pin Anda", pinId, pinUrl)
            }
        }

        // 4. Fitur Like (DIPERBAIKI)
        val likeRef = dbRef.child("Likes").child(pinId)
        btnLike.setOnClickListener {
            if (currentUser == null) return@setOnClickListener
            if (btnLike.tag == "liked") {
                likeRef.child(currentUser.uid).removeValue()
            } else {
                likeRef.child(currentUser.uid).setValue(true)
                // HANYA kirim notifikasi jika yang me-like adalah ORANG LAIN
                if (ownerUid.isNotEmpty() && ownerUid != currentUser.uid) {
                    sendNotification(ownerUid, "menyukai Pin Anda", pinId, pinUrl)
                }
            }
        }

        likeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                tvLikeCount.text = s.childrenCount.toString()
                val isLiked = s.hasChild(currentUser?.uid ?: "")
                btnLike.setImageResource(if (isLiked) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
                btnLike.setColorFilter(if (isLiked) Color.RED else Color.WHITE)
                btnLike.tag = if (isLiked) "liked" else "unliked"
            }
            override fun onCancelled(e: DatabaseError) {}
        })

        // 5. Fitur Simpan Pin (DIPERBAIKI)
        val saveRef = dbRef.child("SavedPins").child(currentUser?.uid ?: "").child(pinId)
        saveRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                if (s.exists()) {
                    btnSave.text = "Tersimpan"
                    btnSave.setBackgroundColor(Color.DKGRAY)
                } else {
                    btnSave.text = "Simpan"
                    btnSave.setBackgroundColor(Color.parseColor("#E60023"))
                }
            }
            override fun onCancelled(e: DatabaseError) {}
        })

        btnSave.setOnClickListener {
            if (currentUser == null) return@setOnClickListener
            saveRef.get().addOnSuccessListener {
                if (it.exists()) {
                    saveRef.removeValue()
                } else {
                    saveRef.setValue(mapOf("id" to pinId, "url" to pinUrl, "title" to pinTitle))
                    // Notifikasi Simpan ke pemilik Pin
                    if (ownerUid.isNotEmpty() && ownerUid != currentUser.uid) {
                        sendNotification(ownerUid, "menyimpan Pin Anda", pinId, pinUrl)
                    }
                }
            }
        }

        // 6. Fitur Komentar (DIPERBAIKI)
        commentAdapter = CommentAdapter(commentList) { tagged -> etComment.setText("@$tagged ") }
        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = commentAdapter

        dbRef.child("Comments").child(pinId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                commentList.clear()
                for (d in s.children) { (d.value as? Map<String, String>)?.let { commentList.add(it) } }
                tvCommentCount.text = commentList.size.toString()
                commentAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(e: DatabaseError) {}
        })

        btnSendComment.setOnClickListener {
            val text = etComment.text.toString().trim()
            if (text.isNotEmpty() && currentUser != null) {
                val map = mapOf("userName" to (currentUser.displayName ?: "User"), "text" to text)
                dbRef.child("Comments").child(pinId).push().setValue(map).addOnSuccessListener {
                    // HANYA kirim notifikasi jika yang komentar ORANG LAIN
                    if (ownerUid.isNotEmpty() && ownerUid != currentUser.uid) {
                        sendNotification(ownerUid, "mengomentari Pin Anda: $text", pinId, pinUrl)
                    }
                    etComment.text.clear()
                }
            }
        }
    }

    private fun downloadImage(url: String) {
        try {
            val req = DownloadManager.Request(Uri.parse(url))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Pin_${System.currentTimeMillis()}.jpg")
            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(req)

            // Notifikasi download biasanya hanya untuk diri sendiri, bisa dihapus agar tidak memenuhi pesan
            Toast.makeText(this, "Mengunduh...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal mengunduh", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendNotification(receiverUid: String, message: String, pinId: String, imageUrl: String) {
        val notif = mapOf(
            "userName" to (currentUser?.displayName ?: "Seseorang"),
            "message" to message,
            "timestamp" to ServerValue.TIMESTAMP,
            "pinId" to pinId,
            "imageUrl" to imageUrl
        )
        // receiverUid adalah pemilik Pin, bukan diri sendiri
        dbRef.child("Notifications").child(receiverUid).push().setValue(notif)
    }
}