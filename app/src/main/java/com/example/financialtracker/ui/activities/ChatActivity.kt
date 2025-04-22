package com.example.financialtracker.ui.activities

import android.os.Bundle
import android.widget.ListView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import com.example.financialtracker.R

class ChatActivity : AppCompatActivity() {

    private lateinit var messagesListView: ListView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var friendName: String

    private val messages = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        friendName = intent.getStringExtra("friend_name") ?: "Unknown Friend"

        messagesListView = findViewById(R.id.messageList)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        // Setup the chat interface
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, messages)
        messagesListView.adapter = adapter

        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
                messageInput.setText("")  // Clear the input field
            }
        }

        // Here you would load the chat history from Firebase or your local database
    }

    private fun sendMessage(message: String) {
        messages.add("You: $message")
        // In real app, you will send the message to Firestore or other database
        (messagesListView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
    }
}
