package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financialtracker.data.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _friendsList = MutableLiveData<List<String>>()
    val friendsList: LiveData<List<String>> = _friendsList

    fun addFriendFromQrCode(
        scannedUid: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRepository.addFriend(scannedUid, onSuccess, onFailure)
    }

    fun loadFriends() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val friendUids = document.get("friends") as? List<*> ?: emptyList<String>()
                if (friendUids.isEmpty()) {
                    _friendsList.value = emptyList()
                    return@addOnSuccessListener
                }

                val usernames = mutableListOf<String>()
                var loaded = 0

                for (uid in friendUids) {
                    if (uid is String) {
                        db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener { friendDoc ->
                                val username = friendDoc.getString("username") ?: "Unknown"
                                usernames.add(username)
                                loaded++
                                if (loaded == friendUids.size) {
                                    _friendsList.value = usernames
                                }
                            }
                            .addOnFailureListener {
                                loaded++
                                if (loaded == friendUids.size) {
                                    _friendsList.value = usernames
                                }
                            }
                    } else {
                        loaded++
                        if (loaded == friendUids.size) {
                            _friendsList.value = usernames
                        }
                    }
                }
            }
    }

    fun removeFriend(
        friendUsername: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRepository.removeFriendByUsername(friendUsername, onSuccess, onFailure)
    }
}