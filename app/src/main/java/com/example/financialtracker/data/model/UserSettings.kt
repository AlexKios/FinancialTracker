package com.example.financialtracker.data.model

data class UserSettings(
    val isDarkMode: Boolean = false,
    val isNotificationsEnabled: Boolean = false,
    val graphSize: Int = 16,
    val language: String = "en"
)
