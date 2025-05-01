package com.example.financialtracker.data.model

import com.google.firebase.Timestamp

data class Chat(
    val chatId: String = "",
    val participants: List<String> = listOf(),
    val timestamp: Timestamp = Timestamp.now()
)