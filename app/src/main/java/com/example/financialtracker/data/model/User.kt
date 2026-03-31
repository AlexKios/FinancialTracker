package com.example.financialtracker.data.model

data class User (
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val online : Boolean = false,
    val name : String = "",
    var budget : Double = 0.0,
    val friends: List<String> = emptyList()
)
