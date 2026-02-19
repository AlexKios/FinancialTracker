package com.example.financialtracker.data.model

data class UserSettings(
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val graphSize: Int = 16,
    val language: String = "en"
)
