package com.example.financialtracker.data.repositories


import android.util.Log
import com.example.financialtracker.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val friendListeners = mutableMapOf<String, ListenerRegistration>()

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

    fun getUserIdByUsername(username: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userId = querySnapshot.documents[0].id
                    onSuccess(userId)
                } else {
                    onFailure(Exception("User not found"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun listenToCurrentUserFriendStatuses(
        onUpdate: (Map<String, Pair<String, String>>) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { doc ->
                val rawList = doc.get("friends") as? List<*> ?: emptyList<Any>()
                val friendUids = rawList.filterIsInstance<String>()
                listenToFriendStatuses(friendUids, onUpdate)
            }
    }

    private fun listenToFriendStatuses(
        friendUids: List<String>,
        onUpdate: (Map<String, Pair<String, String>>) -> Unit // uid -> (username, status)
    ) {
        val friendData = mutableMapOf<String, Pair<String, String>>()

        for (uid in friendUids) {
            val listener = db.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        val username = snapshot.getString("username") ?: "Unknown"
                        val status = snapshot.getString("status") ?: "offline"
                        val online = snapshot.getBoolean("online") ?: false
                        val finalStatus = if (online) "online" else status

                        friendData[uid] = username to finalStatus
                        onUpdate(friendData)
                    }
                }
            friendListeners[uid] = listener
        }
    }

    fun getFriendUsernamesAndStatus(
        onSuccess: (List<Pair<String, String>>) -> Unit,  // Pair<Username, Status>
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onSuccess(emptyList())

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val friendUids = document.get("friends") as? List<*> ?: emptyList<String>()
                if (friendUids.isEmpty()) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                val friendsData = mutableListOf<Pair<String, String>>()
                var loaded = 0

                for (uid in friendUids) {
                    if (uid is String) {
                        db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener { friendDoc ->
                                val username = friendDoc.getString("username") ?: "Unknown"
                                val status = friendDoc.getString("status") ?: "offline"
                                friendsData.add(username to status)
                                loaded++
                                if (loaded == friendUids.size) {
                                    onSuccess(friendsData)
                                }
                            }
                            .addOnFailureListener {
                                loaded++
                                if (loaded == friendUids.size) {
                                    onSuccess(friendsData)
                                }
                            }
                    } else {
                        loaded++
                        if (loaded == friendUids.size) {
                            onSuccess(friendsData)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
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

            if (!newUsername.isNullOrBlank()) updates["username"] = newUsername
            if (!newName.isNullOrBlank()) updates["name"] = newName
            if (!newEmail.isNullOrBlank()) updates["email"] = newEmail

            val userDocRef = db.collection("users").document(userId)

            userDocRef.update(updates)
                .addOnSuccessListener {
                    val emailTasks = if (!newEmail.isNullOrBlank()) {
                        user.verifyBeforeUpdateEmail(newEmail)
                    } else null

                    val passwordTasks = if (!newPassword.isNullOrBlank()) {
                        user.updatePassword(newPassword)
                    } else null

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
    fun removeFriendByUsername(username: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return onFailure(Exception("Not authenticated"))

        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
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
                    }.addOnSuccessListener {
                        onSuccess()
                    }.addOnFailureListener {
                        onFailure(it)
                    }
                } else {
                    onFailure(Exception("Friend not found"))
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUsername(userId: String): Task<DocumentSnapshot> {
        return db.collection("users").document(userId).get()
    }
}