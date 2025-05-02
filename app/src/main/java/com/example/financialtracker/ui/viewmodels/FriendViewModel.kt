package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financialtracker.data.repositories.UserRepository

class FriendsViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val _friendsList = MutableLiveData<List<Pair<String, String>>>()
    val friendsList: LiveData<List<Pair<String, String>>> = _friendsList

    fun addFriendFromQrCode(
        scannedUid: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRepository.addFriend(scannedUid, onSuccess, onFailure)
    }

    fun loadFriends() {
        userRepository.getFriendUsernamesAndStatus(
            onSuccess = { userPairs ->
                _friendsList.value = userPairs
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
        userRepository.listenToCurrentUserFriendStatuses { friendData ->
            _friendsList.postValue(friendData.values.toList())
        }
    }

}