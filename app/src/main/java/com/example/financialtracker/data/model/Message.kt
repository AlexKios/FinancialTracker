package com.example.financialtracker.data.model

data class Message(
    val senderId: String = "",
    val messageContent: String = "",
    val timestamp: Long = 0L,
    val status: String = ""
)