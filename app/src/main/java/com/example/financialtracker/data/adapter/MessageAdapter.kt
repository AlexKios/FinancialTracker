package com.example.financialtracker.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtracker.R
import com.example.financialtracker.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessageAdapter(private var messages: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val senderNameTextView: TextView = itemView.findViewById(R.id.senderName)
        val messageContentTextView: TextView = itemView.findViewById(R.id.textMessage)
        val messageStatusTextView: TextView = itemView.findViewById(R.id.messageStatus)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == 1)
            R.layout.item_message_sent
        else
            R.layout.item_message_received

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        getSenderName(message.senderId) { senderName ->
            holder.senderNameTextView.text = senderName
        }
        holder.messageContentTextView.text = message.messageContent
        holder.messageStatusTextView.text = message.status
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    private fun getSenderName(senderId: String, onResult: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(senderId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val senderName = document.getString("name") ?: "Unknown"
                onResult(senderName)
            } else {
                onResult("Unknown")
            }
        }.addOnFailureListener {
            onResult("Unknown")
        }
    }
}
