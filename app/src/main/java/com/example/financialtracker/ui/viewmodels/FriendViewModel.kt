package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financialtracker.data.repositories.UserRepository

class FriendsViewModel : ViewModel() {
    private val userRepository = UserRepository()
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
        userRepository.getFriendUsernames(
            onSuccess = { usernames ->
                _friendsList.value = usernames
            },
            onFailure = {
                _friendsList.value = emptyList()
            }
        )
    }

    fun removeFriend(
        friendUsername: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRepository.removeFriendByUsername(friendUsername, onSuccess, onFailure)
    }
}