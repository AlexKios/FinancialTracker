package com.example.financialtracker.data.repositories

import com.example.financialtracker.data.model.Chat
import com.example.financialtracker.data.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var globalListener: ListenerRegistration? = null
    private var chatListener: ListenerRegistration? = null

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

    fun listenToAllIncomingMessages(
        currentUserId: String,
        onNewMessage: (Message, String, String) -> Unit
    ) {
        globalListener?.remove()

        db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { chatSnapshots, error ->
                if (error != null || chatSnapshots == null) return@addSnapshotListener

                for (chatDoc in chatSnapshots.documents) {
                    val chatId = chatDoc.id

                    db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(1)
                        .addSnapshotListener { msgSnap, msgError ->
                            if (msgError != null || msgSnap == null) return@addSnapshotListener

                            val message = msgSnap.documents.firstOrNull()?.toObject(Message::class.java)
                            if (message != null && message.senderId != currentUserId && message.status == "sent") {
                                onNewMessage(message, message.senderId!!, chatId)
                            }
                        }
                }
            }.also { globalListener = it }
    }

    fun removeGlobalListener() {
        globalListener?.remove()
        globalListener = null
    }

    fun listenForMessages(
        chatId: String,
        onSuccess: (List<Message>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        chatListener?.remove()

        chatListener = db.collection("chats")
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

    fun stopSingleChatListener() {
        chatListener?.remove()
        chatListener = null
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