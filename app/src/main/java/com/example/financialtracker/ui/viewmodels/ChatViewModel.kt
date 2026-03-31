package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financialtracker.data.helper.ChatSessionTracker
import com.example.financialtracker.data.model.Message
import com.example.financialtracker.data.repositories.ChatRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val chatRepository = ChatRepository()
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages
    private var onNewIncomingMessage: ((Message) -> Unit)? = null

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun listenForMessages(chatId: String, currentUserId: String, friendUid: String) {
        viewModelScope.launch {
            chatRepository.listenForMessages(chatId)
                .catch { _error.value = it.message }
                .collect { newMessages ->
                    _messages.value = newMessages
                    markMessagesAsReceived(chatId, newMessages, currentUserId)

                    val lastIncomingMessage = newMessages.lastOrNull { it.senderId != currentUserId }
                    if (ChatSessionTracker.activeChatUserId != friendUid && lastIncomingMessage != null) {
                        onNewIncomingMessage?.invoke(lastIncomingMessage)
                    }
                }
        }
    }

    fun sendMessage(chatId: String, message: Message) {
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(chatId, message)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun markMessagesAsReceived(chatId: String, messages: List<Message>, currentUserId: String) {
        viewModelScope.launch {
            messages.forEach {
                if (it.senderId != currentUserId && it.status == "sent") {
                    try {
                        chatRepository.updateMessageStatus(chatId, it, "received")
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    fun markMessageAsSeen(chatId: String, message: Message) {
        if (message.status != "seen") {
            viewModelScope.launch {
                try {
                    chatRepository.updateMessageStatus(chatId, message, "seen")
                } catch (e: Exception) {
                }
            }
        }
    }
}
