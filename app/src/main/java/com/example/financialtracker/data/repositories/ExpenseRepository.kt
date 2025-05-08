package com.example.financialtracker.data.repositories

import com.example.financialtracker.data.model.Expense
import com.google.firebase.firestore.FirebaseFirestore

class ExpenseRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getExpenseById(
        id: String,
        onSuccess: (Expense) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("expenses").document(id)
            .get()
            .addOnSuccessListener { document ->
                val expense = document.toObject(Expense::class.java)
                if (expense != null) {
                    onSuccess(expense)
                } else {
                    onFailure(Exception("Expense not found"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}