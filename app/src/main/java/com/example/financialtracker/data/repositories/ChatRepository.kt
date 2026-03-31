package com.example.financialtracker.data.repositories

import com.example.financialtracker.data.model.Chat
import com.example.financialtracker.data.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun createChat(friendUid: String): String {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User is not authenticated")
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

        db.collection("chats").document(chatId).set(chat).await()
        return chatId
    }

    suspend fun sendMessage(chatId: String, message: Message) {
        val messageId = db.collection("chats").document(chatId).collection("messages").document().id
        
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .set(message)
            .await()

        db.collection("chats")
            .document(chatId)
            .update("timestamp", message.timestamp)
            .await()
            
        updateMessageStatus(chatId, message, "sent")
    }

    fun listenToAllIncomingMessages(currentUserId: String): Flow<Triple<Message, String, String>> = callbackFlow {
        val registration = db.collection("chats")
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
                                trySend(Triple(message, message.senderId!!, chatId))
                            }
                        }
                }
            }
        awaitClose { registration.remove() }
    }

    fun listenForMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val registration = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshots?.documents?.mapNotNull {
                    it.toObject(Message::class.java)
                } ?: emptyList()

                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    suspend fun updateMessageStatus(chatId: String, message: Message, newStatus: String) {
        val result = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .whereEqualTo("timestamp", message.timestamp)
            .get()
            .await()
            
        for (document in result.documents) {
            document.reference.update("status", newStatus).await()
        }
    }
}
