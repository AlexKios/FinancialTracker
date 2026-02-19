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

    fun loadUserSettings() {
        viewModelScope.launch {
            settingsRepository.getUserSettings(
                onSuccess = { settings ->
                    _userSettings.value = settings
                },
                onFailure = { 
                    // Handle error, maybe expose an error state
                }
            )
        }
    }

    fun saveUserSettings(settings: UserSettings, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            settingsRepository.saveUserSettings(settings,
                onSuccess = {
                    _userSettings.value = settings
                    onSuccess()
                },
                onFailure = onFailure
            )
        }
    }
}
