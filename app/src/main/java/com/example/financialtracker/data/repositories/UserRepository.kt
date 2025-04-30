package com.example.financialtracker.data.repositories


import android.util.Log
import com.example.financialtracker.data.model.Friend
import com.example.financialtracker.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUser(onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onFailure(Exception("User not found"))
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onFailure(Exception("User is not authenticated"))
        }
    }

    fun getFriends(onSuccess: (List<Friend>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        val friends = user.friends.map { Friend(it, "") }  // Populate with username if needed
                        onSuccess(friends)
                    } else {
                        onFailure(Exception("User not found"))
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onFailure(Exception("User is not authenticated"))
        }
    }

    fun addFriend(friendUid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
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
            }.addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
        } else {
            onFailure(Exception("User is not authenticated"))
        }
    }

    fun registerUser(
        name: String,
        email: String,
        password: String,
        username: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val user = User(
                            uid = userId,
                            username = username,
                            name = name,
                            email = email,
                            friends = listOf()  // start with empty friends list
                        )
                        db.collection("users")
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                auth.currentUser?.sendEmailVerification()
                                    ?.addOnSuccessListener {
                                        Log.d("FirebaseAuth", "Verification email sent.")
                                        onSuccess()
                                    }
                                    ?.addOnFailureListener { e ->
                                        Log.e("FirebaseAuth", "Error sending verification email", e)
                                        onFailure(e)
                                    }
                            }
                            .addOnFailureListener { e ->
                                onFailure(e)
                            }
                    } else {
                        onFailure(Exception("Failed to get user ID after registration"))
                    }
                } else {
                    task.exception?.let {
                        onFailure(it)
                    }
                }
            }
    }

    fun loginUser(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        onSuccess()
                    } else {
                        onFailure(Exception("Email not verified"))
                    }
                } else {
                    task.exception?.let {
                        onFailure(it)
                    }
                }
            }
    }
}