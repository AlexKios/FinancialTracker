package com.example.financialtracker.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.financialtracker.R
import com.example.financialtracker.data.adapter.MessageAdapter
import com.example.financialtracker.data.helper.ChatSessionTracker
import com.example.financialtracker.data.helper.NotificationHelper
import com.example.financialtracker.data.model.Message
import com.example.financialtracker.data.repositories.ChatRepository
import com.example.financialtracker.ui.viewmodels.ChatViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {

    private lateinit var messageInput: EditText
    private lateinit var sendButton: FloatingActionButton
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
        val profileImageUrl = intent.getStringExtra("friend_profile_image_url") ?: ""

        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        val toolbarAvatar = findViewById<TextView>(R.id.toolbarAvatar)
        val toolbarAvatarImage = findViewById<ImageView>(R.id.toolbarAvatarImage)
        
        toolbarTitle.text = friendName

        if (profileImageUrl.isNotEmpty()) {
            toolbarAvatarImage.visibility = View.VISIBLE
            toolbarAvatar.visibility = View.GONE
            Glide.with(this)
                .load(profileImageUrl)
                .circleCrop()
                .into(toolbarAvatarImage)
        } else {
            toolbarAvatarImage.visibility = View.GONE
            toolbarAvatar.visibility = View.VISIBLE
            toolbarAvatar.text = getInitials(friendName)
        }

        messagesRecyclerView = findViewById(R.id.messageList)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        messageAdapter = MessageAdapter(emptyList())
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        messagesRecyclerView.layoutManager = layoutManager
        messagesRecyclerView.adapter = messageAdapter
        notificationHelper = NotificationHelper(this)

        val friendUid = intent.getStringExtra("friend_uid")!!
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

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

            if (messages.isNotEmpty()) {
                messagesRecyclerView.scrollToPosition(messages.size - 1)
            }
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

    private fun getInitials(name: String): String {
        val parts = name.split(" ")
        return if (parts.size >= 2) {
            (parts[0].take(1) + parts[1].take(1)).uppercase()
        } else {
            name.take(2).uppercase()
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