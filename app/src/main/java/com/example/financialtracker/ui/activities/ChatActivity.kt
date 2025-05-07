package com.example.financialtracker.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtracker.R
import com.example.financialtracker.data.adapter.MessageAdapter
import com.example.financialtracker.data.helper.ChatSessionTracker
import com.example.financialtracker.data.helper.NotificationHelper
import com.example.financialtracker.data.model.Message
import com.example.financialtracker.data.repositories.ChatRepository
import com.example.financialtracker.ui.viewmodels.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {

    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var friendName: String
    private lateinit var viewModel: ChatViewModel
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatId: String
    private lateinit var currentUserId: String
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        friendName = intent.getStringExtra("friend_name") ?: "Unknown Friend"

        messagesRecyclerView = findViewById(R.id.messageList)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        messageAdapter = MessageAdapter(emptyList())
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = messageAdapter
        notificationHelper = NotificationHelper(this)

        val friendUid = intent.getStringExtra("friend_uid")!!
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val messageInput = findViewById<EditText>(R.id.messageInput)
        val sendButton = findViewById<Button>(R.id.sendButton)

        viewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[ChatViewModel::class.java]

        val repo = ChatRepository()
        repo.createChat(friendUid, { generatedChatId ->
            chatId = generatedChatId
            viewModel.listenForMessages(chatId, currentUserId, friendUid)
        }, {
            Toast.makeText(this, "Failed to get chat", Toast.LENGTH_SHORT).show()
        })

        viewModel.messages.observe(this) { messages ->
            messageAdapter.updateMessages(messages)

            messages.filter { it.senderId != currentUserId }.forEach {
                viewModel.markMessageAsSeen(chatId, it)
            }

            messagesRecyclerView.scrollToPosition(messages.size - 1)
        }

        viewModel.setOnNewMessageListener {}

        ChatSessionTracker.activeChatUserId = friendUid

        sendButton.setOnClickListener {
            val text = messageInput.text.toString()
            if (text.isNotBlank()) {
                val msg = Message(senderId = currentUserId, messageContent = text)
                viewModel.sendMessage(chatId, msg)
                messageInput.text.clear()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ChatSessionTracker.activeChatUserId = null
        viewModel.stopSingleChatListener()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("draftMessage", messageInput.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val draft = savedInstanceState.getString("draftMessage", "")
        messageInput.setText(draft)
    }
}