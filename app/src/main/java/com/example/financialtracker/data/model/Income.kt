package com.example.financialtracker.data.model

import com.google.firebase.Timestamp

data class Income(
    val id: String = "",
    val amount: Double = 0.0,
    val date: Timestamp? = null,
    val source: String = "",
    val isRecurring: Boolean = false,
    val recurringDate: Timestamp? = null
)