package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financialtracker.data.helper.ChatSessionTracker
import com.example.financialtracker.data.model.Message
import com.example.financialtracker.data.repositories.ChatRepository

class ChatViewModel : ViewModel() {

    private val chatRepository = ChatRepository()
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages
    private var onNewIncomingMessage: ((Message) -> Unit)? = null

    private val _error = MutableLiveData<String>()

    fun listenForMessages(chatId: String, currentUserId: String, friendUid: String) {
        chatRepository.listenForMessages(chatId, { newMessages ->
            _messages.value = newMessages

            markMessagesAsReceived(chatId, newMessages, currentUserId)

            val lastIncomingMessage = newMessages.lastOrNull { it.senderId != currentUserId }

            if (ChatSessionTracker.activeChatUserId != friendUid && lastIncomingMessage != null) {
                onNewIncomingMessage?.invoke(lastIncomingMessage)
            }

        }, {
            _error.value = it.message
        })
    }

    fun sendMessage(chatId: String, message: Message) {
        chatRepository.sendMessage(chatId, message, {}, {
            _error.value = it.message
        })
    }

    private fun markMessagesAsReceived(chatId: String, messages: List<Message>, currentUserId: String) {
        messages.forEach {
            if (it.senderId != currentUserId && it.status == "sent") {
                chatRepository.updateMessageStatus(chatId, it, "received")
            }
        }
    }

    fun markMessageAsSeen(chatId: String, message: Message) {
        if (message.status != "seen") {
            chatRepository.updateMessageStatus(chatId, message, "seen")
        }
    }

    fun setOnNewMessageListener(callback: (Message) -> Unit) {
        onNewIncomingMessage = callback
    }

    fun stopSingleChatListener() {
        chatRepository.stopSingleChatListener()
    }
}
