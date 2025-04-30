package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financialtracker.data.repositories.UserRepository

class RegisterViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _registrationSuccess = MutableLiveData<Boolean>()
    val registrationSuccess: LiveData<Boolean> get() = _registrationSuccess

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun registerUser(name: String, username: String, email: String, password: String) {
        userRepository.registerUser(name, email, password, username,
            onSuccess = {
                _registrationSuccess.postValue(true)
            },
            onFailure = { exception ->
                _errorMessage.postValue(exception.message)
            }
        )
    }
}
