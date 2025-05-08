package com.example.financialtracker.data.model

import com.google.firebase.Timestamp

data class Expense(
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: Timestamp? = null,
    val isRecurring: Boolean = false,
    val recurringDate: Timestamp? = null
)