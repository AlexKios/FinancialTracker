package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financialtracker.data.repositories.UserRepository

class LoginViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun login(email: String, password: String) {
        userRepository.loginUser(email, password,
            onSuccess = {
                _loginSuccess.value = true
            },
            onFailure = { exception ->
                _errorMessage.value = exception.message
            }
        )
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
