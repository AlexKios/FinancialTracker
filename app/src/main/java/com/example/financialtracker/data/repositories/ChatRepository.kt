package com.example.financialtracker.data.repositories

import com.example.financialtracker.data.model.Chat
import com.example.financialtracker.data.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun createChat(friendUid: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            val chatId = if (currentUserId < friendUid) {
                "${currentUserId}_$friendUid"
            } else {
                "${friendUid}_$currentUserId"
            }

            val chat = Chat(
                chatId = chatId,
                participants = listOf(currentUserId, friendUid),
                timestamp = Timestamp(System.currentTimeMillis() / 1000, 0)
            )

            db.collection("chats")
                .document(chatId)
                .set(chat)
                .addOnSuccessListener {
                    onSuccess(chatId)
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onFailure(Exception("User is not authenticated"))
        }
    }

    fun sendMessage(chatId: String, message: Message, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            val messageId = db.collection("chats").document(chatId).collection("messages").document().id
            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .set(message)
                .addOnSuccessListener {
                    db.collection("chats")
                        .document(chatId)
                        .update("timestamp", message.timestamp)
                        .addOnSuccessListener {
                            updateMessageStatus(chatId, message, "sent")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            onFailure(e)
                        }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onFailure(Exception("User is not authenticated"))
        }
    }

    fun listenForMessages(chatId: String, onSuccess: (List<Message>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                try {
                    val messages = snapshots?.documents?.mapNotNull {
                        it.toObject(Message::class.java)
                    } ?: emptyList()

                    onSuccess(messages)
                } catch (e: Exception) {
                    onFailure(e)
                }
            }
    }

    fun updateMessageStatus(chatId: String, message: Message, newStatus: String) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .whereEqualTo("timestamp", message.timestamp)
            .get()
            .addOnSuccessListener { result ->
                for (document in result.documents) {
                    document.reference.update("status", newStatus)
                }
            }
    }
}