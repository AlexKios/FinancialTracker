package com.example.financialtracker.data.model

data class User (
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val isOnline : Boolean = false,
    val name : String = "",
    val budget : Double = 0.0,
    val incomeIds: List<String> = emptyList(),
    val expenseIds: List<String> = emptyList(),
    val friends: List<String> = emptyList()
)