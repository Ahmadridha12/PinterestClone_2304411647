package com.example.hands10_firebase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommentAdapter(
    private val comments: List<Map<String, String>>,
    private val onReplyClick: (String) -> Unit // Fungsi untuk handle klik balas
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvName = v.findViewById<TextView>(R.id.tvCommentName)
        val tvText = v.findViewById<TextView>(R.id.tvCommentText)
        val btnReply = v.findViewById<TextView>(R.id.btnReplyComment)
        val layoutReplies = v.findViewById<LinearLayout>(R.id.layoutReplies)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvName.text = comment["userName"]
        holder.tvText.text = comment["text"]

        holder.btnReply.setOnClickListener {
            // Mengirim nama user yang akan dibalas ke Activity
            onReplyClick(comment["userName"] ?: "")
        }
    }

    override fun getItemCount() = comments.size
}