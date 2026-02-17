package com.example.financialtracker.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financialtracker.data.model.User
import com.example.financialtracker.data.repositories.UserRepository

class AccountViewModel: ViewModel() {

    private val userRepo = UserRepository()
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    private val _uploadResult = MutableLiveData<Result<String>>()
    val uploadResult: LiveData<Result<String>> = _uploadResult

    fun loadCurrentUser() {
        userRepo.getCurrentUser(
            onSuccess = { user ->
                _currentUser.postValue(user)
            },
            onFailure = { error ->
                _updateResult.postValue(Result.failure(error))
            }
        )
    }

    fun updateProfile(
        newUsername: String? = null,
        newEmail:    String? = null,
        newPassword: String? = null,
        newName:     String? = null
    ) {
        userRepo.updateUserData(
            newUsername = newUsername,
            newEmail    = newEmail,
            newPassword = newPassword,
            newName     = newName,
            onSuccess = {
                _updateResult.postValue(Result.success(Unit))

                loadCurrentUser()
            },
            onFailure = { exception ->
                _updateResult.postValue(Result.failure(exception))
            }
        )
    }

    fun uploadProfilePicture(uri: Uri) {
        userRepo.updateUserProfilePicture(uri.toString(),
            onSuccess = {
                loadCurrentUser()
                _uploadResult.postValue(Result.success(uri.toString()))
            },
            onFailure = { exception ->
                _uploadResult.postValue(Result.failure(exception))
            }
        )
    }
}