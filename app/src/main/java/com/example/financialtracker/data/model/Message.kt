package com.example.financialtracker.data.model

import com.google.firebase.Timestamp

data class Message(
    val senderId: String = "",
    val messageContent: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = ""
)