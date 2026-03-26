package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financialtracker.data.model.Friend
import com.example.financialtracker.data.repositories.UserRepository

class FriendsViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val _friendsList = MutableLiveData<List<Friend>>()
    val friendsList: LiveData<List<Friend>> = _friendsList

    fun addFriendFromQrCode(
        scannedUid: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRepository.addFriend(scannedUid, onSuccess, onFailure)
    }

    fun loadFriends() {
        userRepository.getFriendsDetailed(
            onSuccess = { friends ->
                _friendsList.value = friends
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

    fun startListeningToFriendStatuses() {
        userRepository.listenToCurrentUserFriendStatuses { friends ->
            _friendsList.postValue(friends)
        }
    }

}