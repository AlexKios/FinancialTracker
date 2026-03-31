package com.example.financialtracker.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financialtracker.data.model.User
import com.example.financialtracker.data.repositories.UserRepository
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {

    private val userRepo = UserRepository()
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    private val _uploadResult = MutableLiveData<Result<String>>()
    val uploadResult: LiveData<Result<String>> = _uploadResult

    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userRepo.getCurrentUser()
                _currentUser.value = user
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            }
        }
    }

    fun updateProfile(
        newUsername: String? = null,
        newEmail: String? = null,
        newPassword: String? = null,
        newName: String? = null
    ) {
        viewModelScope.launch {
            try {
                userRepo.updateUserData(newUsername, newEmail, newPassword, newName)
                _updateResult.value = Result.success(Unit)
                loadCurrentUser()
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            }
        }
    }

    fun uploadProfilePicture(uri: Uri) {
        viewModelScope.launch {
            try {
                val imageUrl = userRepo.updateUserProfilePicture(uri)
                loadCurrentUser()
                _uploadResult.value = Result.success(imageUrl)
            } catch (e: Exception) {
                _uploadResult.value = Result.failure(e)
            }
        }
    }
}
