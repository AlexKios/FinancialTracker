package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financialtracker.data.model.Friend
import com.example.financialtracker.data.repositories.UserRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class FriendsViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val _friendsList = MutableLiveData<List<Friend>>()
    val friendsList: LiveData<List<Friend>> = _friendsList

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun addFriendFromQrCode(scannedUid: String) {
        viewModelScope.launch {
            try {
                userRepository.addFriend(scannedUid)
                loadFriends()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadFriends() {
        viewModelScope.launch {
            try {
                val friends = userRepository.getFriends()
                _friendsList.value = friends
            } catch (e: Exception) {
                _friendsList.value = emptyList()
                _error.value = e.message
            }
        }
    }

    fun removeFriend(friendUsername: String) {
        viewModelScope.launch {
            try {
                userRepository.removeFriendByUsername(friendUsername)
                loadFriends()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun startListeningToFriendStatuses() {
        viewModelScope.launch {
            userRepository.listenToFriendsStatuses()
                .catch { _error.value = it.message }
                .collect { friends ->
                    _friendsList.value = friends
                }
        }
    }
}
