package com.example.hands10_firebase

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class EditPinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pin)

        val pinId = intent.getStringExtra("ID") ?: ""
        val oldTitle = intent.getStringExtra("TITLE") ?: ""
        val oldUrl = intent.getStringExtra("URL") ?: ""

        val etTitle = findViewById<EditText>(R.id.etEditTitle)
        val etUrl = findViewById<EditText>(R.id.etEditUrl)
        val btnUpdate = findViewById<Button>(R.id.btnUpdatePin)

        etTitle.setText(oldTitle)
        etUrl.setText(oldUrl)

        btnUpdate.setOnClickListener {
            val newTitle = etTitle.text.toString().trim()
            val newUrl = etUrl.text.toString().trim()

            if (newTitle.isNotEmpty() && newUrl.isNotEmpty()) {
                val pinRef = FirebaseDatabase.getInstance().getReference("Pins").child(pinId)

                // Update semua kemungkinan nama field agar sinkron
                val updateData = mapOf(
                    "title" to newTitle,
                    "url" to newUrl,
                    "imageUrl" to newUrl
                )

                pinRef.updateChildren(updateData).addOnSuccessListener {
                    Toast.makeText(this, "Pin berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Kolom tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }
}