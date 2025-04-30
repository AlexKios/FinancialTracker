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

    fun updateUserData(
        newUsername: String?,
        newEmail: String?,
        newPassword: String?,
        newName: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser
        val userId = user?.uid

        if (user != null && userId != null) {
            val updates = mutableMapOf<String, Any>()

            // Add Firestore updates
            if (!newUsername.isNullOrBlank()) updates["username"] = newUsername
            if (!newName.isNullOrBlank()) updates["name"] = newName
            if (!newEmail.isNullOrBlank()) updates["email"] = newEmail

            val userDocRef = db.collection("users").document(userId)

            // Start batched updates
            userDocRef.update(updates)
                .addOnSuccessListener {
                    // Handle email/password updates
                    val emailTasks = if (!newEmail.isNullOrBlank()) {
                        user.updateEmail(newEmail)
                    } else null

                    val passwordTasks = if (!newPassword.isNullOrBlank()) {
                        user.updatePassword(newPassword)
                    } else null

                    // Wait for both email and password updates to complete (if needed)
                    if (emailTasks != null && passwordTasks != null) {
                        emailTasks.addOnSuccessListener {
                            passwordTasks.addOnSuccessListener {
                                onSuccess()
                            }.addOnFailureListener { onFailure(it) }
                        }.addOnFailureListener { onFailure(it) }
                    } else if (emailTasks != null) {
                        emailTasks.addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onFailure(it) }
                    } else if (passwordTasks != null) {
                        passwordTasks.addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onFailure(it) }
                    } else {
                        onSuccess()
                    }
                }
                .addOnFailureListener { onFailure(it) }
        } else {
            onFailure(Exception("User not authenticated"))
        }
    }
}