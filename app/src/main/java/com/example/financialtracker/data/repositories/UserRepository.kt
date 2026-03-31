package com.example.financialtracker.data.repositories

import android.net.Uri
import android.util.Log
import com.example.financialtracker.data.helper.CloudinaryClient
import com.example.financialtracker.data.model.User
import com.example.financialtracker.data.model.Friend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getFriends(): List<Friend> {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        val userDoc = db.collection("users").document(currentUserId).get().await()
        val friendUids = (userDoc.get("friends") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

        return friendUids.mapNotNull { friendUid ->
            try {
                val friendDoc = db.collection("users").document(friendUid).get().await()
                val username = friendDoc.getString("username")
                val profileImageUrl = friendDoc.getString("profileImageUrl") ?: ""
                val online = friendDoc.getBoolean("online") ?: false
                if (username != null) {
                    Friend(
                        userId = friendDoc.id,
                        username = username,
                        profileImageUrl = profileImageUrl,
                        online = online
                    )
                } else null
            } catch (e: Exception) {
                Log.e("UserRepository", "Error fetching friend details for UID: $friendUid", e)
                null
            }
        }
    }

    suspend fun updateUserProfilePicture(imageUri: Uri): String {
        return suspendCancellableCoroutine { continuation ->
            CloudinaryClient.uploadImage(
                uri = imageUri,
                uploadPreset = "DipProject",
                onSuccess = { imageUrl ->
                    val sanitizedUrl = imageUrl.trim()
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        db.collection("users").document(userId)
                            .update("profileImageUrl", sanitizedUrl)
                            .addOnSuccessListener { continuation.resume(sanitizedUrl) }
                            .addOnFailureListener { continuation.resumeWithException(it) }
                    } else {
                        continuation.resumeWithException(Exception("User not authenticated"))
                    }
                },
                onError = { errorMessage ->
                    continuation.resumeWithException(Exception(errorMessage))
                }
            )
        }
    }

    suspend fun getCurrentUser(): User {
        val userId = auth.currentUser?.uid ?: throw Exception("User is not authenticated")
        val document = db.collection("users").document(userId).get().await()
        return document.toObject(User::class.java) ?: throw Exception("User not found")
    }

    suspend fun getUserById(userId: String): User {
        val document = db.collection("users").document(userId).get().await()
        return document.toObject(User::class.java) ?: throw Exception("User not found")
    }

    fun listenToFriendsStatuses(): Flow<List<Friend>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: run {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }

        val friendData = mutableMapOf<String, Friend>()
        val friendListeners = mutableMapOf<String, ListenerRegistration>()

        val userListener = db.collection("users").document(currentUserId)
            .addSnapshotListener { doc, _ ->
                val friendUids = (doc?.get("friends") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                val currentListeners = friendListeners.keys.toSet()
                currentListeners.subtract(friendUids.toSet()).forEach { uidToRemove ->
                    friendListeners[uidToRemove]?.remove()
                    friendListeners.remove(uidToRemove)
                    friendData.remove(uidToRemove)
                }

                friendUids.forEach { uid ->
                    if (!friendListeners.containsKey(uid)) {
                        val listener = db.collection("users").document(uid)
                            .addSnapshotListener { snapshot, _ ->
                                if (snapshot != null && snapshot.exists()) {
                                    val username = snapshot.getString("username") ?: "Unknown"
                                    val online = snapshot.getBoolean("online") ?: false
                                    val profileImageUrl = snapshot.getString("profileImageUrl") ?: ""

                                    friendData[uid] = Friend(
                                        userId = uid, 
                                        username = username, 
                                        profileImageUrl = profileImageUrl, 
                                        online = online
                                    )
                                    trySend(friendData.values.toList().sortedBy { it.username })
                                }
                            }
                        friendListeners[uid] = listener
                    }
                }
            }

        awaitClose {
            userListener.remove()
            friendListeners.values.forEach { it.remove() }
            friendListeners.clear()
        }
    }

    suspend fun addFriend(friendUid: String) {
        val userId = auth.currentUser?.uid ?: throw Exception("User is not authenticated")
        val userRef = db.collection("users").document(userId)
        val friendRef = db.collection("users").document(friendUid)

        db.runTransaction { transaction ->
            val userDoc = transaction.get(userRef)
            val friendDoc = transaction.get(friendRef)

            val currentFriends = userDoc.toObject(User::class.java)?.friends?.toMutableList() ?: mutableListOf()
            if (!currentFriends.contains(friendUid)) {
                currentFriends.add(friendUid)
                transaction.update(userRef, "friends", currentFriends)
            }

            val friendFriends = friendDoc.toObject(User::class.java)?.friends?.toMutableList() ?: mutableListOf()
            if (!friendFriends.contains(userId)) {
                friendFriends.add(userId)
                transaction.update(friendRef, "friends", friendFriends)
            }
        }.await()
    }

    suspend fun removeFriendByUsername(username: String) {
        val currentUid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
        val querySnapshot = db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .await()

        if (querySnapshot.isEmpty) throw Exception("Friend not found")
        val friendUid = querySnapshot.documents[0].id

        db.runTransaction { transaction ->
            val userRef = db.collection("users").document(currentUid)
            val friendRef = db.collection("users").document(friendUid)

            val userFriends = transaction.get(userRef).toObject(User::class.java)?.friends?.toMutableList() ?: mutableListOf()
            val friendFriends = transaction.get(friendRef).toObject(User::class.java)?.friends?.toMutableList() ?: mutableListOf()

            userFriends.remove(friendUid)
            friendFriends.remove(currentUid)

            transaction.update(userRef, "friends", userFriends)
            transaction.update(friendRef, "friends", friendFriends)
        }.await()
    }

    suspend fun updateBudget(budget: Double) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        db.collection("users").document(userId).update("budget", budget).await()
    }

    suspend fun registerUser(name: String, email: String, password: String, username: String) {
        val task = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = task.user?.uid ?: throw Exception("Failed to get user ID after registration")
        
        val user = User(
            uid = userId,
            username = username,
            name = name,
            email = email,
            friends = listOf()
        )
        db.collection("users").document(userId).set(user).await()
        auth.currentUser?.sendEmailVerification()?.await()
    }

    suspend fun loginUser(email: String, password: String) {
        val task = auth.signInWithEmailAndPassword(email, password).await()
        val user = task.user
        if (user == null || !user.isEmailVerified) {
            throw Exception("Email not verified")
        }
    }

    suspend fun updateUserData(newUsername: String?, newEmail: String?, newPassword: String?, newName: String?) {
        val user = auth.currentUser ?: throw Exception("User not authenticated")
        val userId = user.uid
        val updates = mutableMapOf<String, Any>()

        if (!newUsername.isNullOrBlank()) updates["username"] = newUsername
        if (!newName.isNullOrBlank()) updates["name"] = newName
        if (!newEmail.isNullOrBlank()) updates["email"] = newEmail

        if (updates.isNotEmpty()) {
            db.collection("users").document(userId).update(updates).await()
        }

        if (!newEmail.isNullOrBlank()) {
            user.verifyBeforeUpdateEmail(newEmail).await()
        }
        if (!newPassword.isNullOrBlank()) {
            user.updatePassword(newPassword).await()
        }
    }
}
