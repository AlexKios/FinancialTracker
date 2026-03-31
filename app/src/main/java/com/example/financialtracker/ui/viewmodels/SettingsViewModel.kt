package com.example.financialtracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financialtracker.data.model.UserSettings
import com.example.financialtracker.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val settingsRepository = SettingsRepository()

    private val _userSettings = MutableStateFlow<UserSettings?>(null)
    val userSettings: StateFlow<UserSettings?> = _userSettings

    private val _saveSuccess = MutableStateFlow<Boolean>(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadUserSettings() {
        viewModelScope.launch {
            try {
                val settings = settingsRepository.getUserSettings()
                _userSettings.value = settings
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun saveUserSettings(settings: UserSettings) {
        viewModelScope.launch {
            try {
                settingsRepository.saveUserSettings(settings)
                _userSettings.value = settings
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
