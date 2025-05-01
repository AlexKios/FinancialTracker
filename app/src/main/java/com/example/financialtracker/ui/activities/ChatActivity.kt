package com.example.financialtracker.ui.activities

import android.os.Bundle
import android.widget.ListView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.financialtracker.R
import com.example.financialtracker.data.model.Message
import com.example.financialtracker.data.repositories.ChatRepository
import com.example.financialtracker.ui.viewmodels.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {

    private lateinit var messagesListView: ListView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var friendName: String
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ArrayAdapter<String>
    private val displayMessages = mutableListOf<String>()

    private lateinit var chatId: String
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        friendName = intent.getStringExtra("friend_name") ?: "Unknown Friend"

        messagesListView = findViewById(R.id.messageList)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        val friendUid = intent.getStringExtra("friend_uid")!!
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        messagesListView = findViewById(R.id.messageList)
        val messageInput = findViewById<EditText>(R.id.messageInput)
        val sendButton = findViewById<Button>(R.id.sendButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayMessages)
        messagesListView.adapter = adapter

        viewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[ChatViewModel::class.java]

        val repo = ChatRepository()
        repo.createChat(friendUid, { generatedChatId ->
            chatId = generatedChatId
            viewModel.listenForMessages(chatId, currentUserId)
        }, {
            Toast.makeText(this, "Failed to get chat", Toast.LENGTH_SHORT).show()
        })

        viewModel.messages.observe(this) { messages ->
            displayMessages.clear()
            displayMessages.addAll(messages.map {
                val prefix = if (it.senderId == currentUserId) "You" else "Friend"
                "$prefix: ${it.messageContent} [${it.status}]"
            })
            adapter.notifyDataSetChanged()

            messages.filter { it.senderId != currentUserId }.forEach {
                viewModel.markMessageAsSeen(chatId, it)
            }
        }

        sendButton.setOnClickListener {
            val text = messageInput.text.toString()
            if (text.isNotBlank()) {
                val msg = Message(senderId = currentUserId, messageContent = text)
                viewModel.sendMessage(chatId, msg)
                messageInput.text.clear()
            }
        }
    }
}