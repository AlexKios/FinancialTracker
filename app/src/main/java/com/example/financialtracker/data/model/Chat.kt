package com.example.financialtracker.data.model

data class Chat(
    val chatId: String = "",
    val participants: List<String> = listOf(),
    val timestamp: Long = 0L,
)